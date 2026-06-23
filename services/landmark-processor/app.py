from flask import Flask, request, jsonify
from landmark_preprocessor import LandmarkPreprocessor
from landmark_meta_generator import LandmarkMetaGenerator
from geopy.geocoders import Nominatim
from pymongo import MongoClient
from dotenv import load_dotenv

import uuid

from typing import Tuple
from flask.wrappers import Response

def error_response(code: str, message: str, retryable: bool = False, status: int = 500) -> Tuple[Response, int]:
    return jsonify({
        "code": code,
        "message": message,
        "correlationId": str(uuid.uuid4()),
        "retryable": retryable
    }), status

import os

app = Flask(__name__)


load_dotenv(override=True)
MONGO_URL = os.getenv("MONGO_URL", "mongodb://localhost:27017")
DB_NAME   = os.getenv("MONGO_DB",  "scavengerhunt")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"}), 200

@app.route("/resolve-city", methods=["POST"])
def resolve_city():
    data = request.get_json()
    lat = data.get("latitude")
    lng = data.get("longitude")

    if lat is None or lng is None:
        return error_response("MISSING_COORDINATES", "Missing latitude/longitude", status=400)

    geolocator = Nominatim(user_agent="scavenger-agent")
    try:
        location = geolocator.reverse(f"{lat}, {lng}", language='en')
        if not location:
            return error_response("RESOLVE_COORDINATES_ERROR", "Could not resolve location", status=400)

        city = location.raw.get("address", {}).get("city") \
            or location.raw.get("address", {}).get("town") \
            or location.raw.get("address", {}).get("village")

        if not city:
            return error_response("CITY_NOT_FOUND", "City not found in location data", status=400)

        print(f"[ResolveCity] Resolved: {city}")
        return jsonify({"status": "ok", "city": city})

    except Exception as e:
        print(f"[ResolveCity] Error: {e}")
        return error_response("INTERNAL_ERROR", str(e), status=500)

@app.route("/fetch-landmark", methods=["POST"])
def fetch_landmark():
    data = request.get_json()
    lat = data.get("latitude")
    lng = data.get("longitude")
    if lat is None or lng is None:
        return error_response("MISSING_COORDINATES", "Missing latitude/longitude", status=400)


    # use internal resolve_city() function instead of duplicating logic
    with app.test_request_context('/resolve-city', method='POST', json={"latitude": lat, "longitude": lng}):
        resolve_response, status_code = resolve_city()

    if status_code != 200:
        return error_response("RESOLVE_CITY_ERROR",  "Failed to resolve city", status=400)

    city = resolve_response.get_json()["city"]
    print(f"[Landmark Processor] Resolved city: {city}")

    # check MongoDB
    client = MongoClient(MONGO_URL)
    db = client[DB_NAME]
    collection = db["landmarks"]

    existing_count = collection.count_documents({"city": city})
    print(f"[Landmark Processor] Found {existing_count} landmarks for city {city} in DB")

    # if existing_count > 20:
    #     print(f"[✓] Landmark data for {city} already initialized, skipping fetch.")
    #     return jsonify({"status": "ok", "city": city})

    print(f"[!] Landmark data for {city} appears incomplete ({existing_count}), proceeding with fetch...")

    query = f"""
    [out:json];
    area["name"="{city}"]["boundary"="administrative"]->.searchArea;

    (
        way["amenity"]["name"]["amenity"!="parking"]["amenity"!="parking_space"]["amenity"!="bicycle_parking"]["amenity"!="waste_disposal"](area.searchArea);
        way["tourism"]["name"]["tourism"!="guest_house"](area.searchArea);
        way["historic"]["name"](area.searchArea);
        way["leisure"]["name"]["leisure"!="pitch"](area.searchArea);
        way["building"]["name"](area.searchArea);
    );
    out geom;
    """

    try:
        LandmarkPreprocessor(query, city=city)\
            .fetchRaw()\
            .findRawLandmarks()\
            .processRawLandmark()\
            .storeToDB(overwrite=False, mongo_url=MONGO_URL)
            # .removeDuplicates()

        return jsonify({"status": "ok", "city": city})

    except Exception as e:
        print(f"[Landmark Processor] Landmark processing failed: {e}")
        return error_response("INTERNAL_ERROR", str(e), status=500)

@app.route("/generate-landmark-meta", methods=["POST"])
def generate_landmark_meta():

    data = request.get_json(force=True) or {}
    landmark_ids = data.get("landmarkIds") or []
    override = bool(data.get("force", False))

    if not landmark_ids or not isinstance(landmark_ids, list):
        return error_response("EMPTY_LANDMARK_LIST", "landmarkIds must be a non-empty list", status=400)

    client = MongoClient(MONGO_URL)
    db = client[DB_NAME]

    if not override:
        existing_ids = db["landmark_metadata"].find(
            {"landmarkId": {"$in": landmark_ids}},
            {"landmarkId": 1, "_id": 0}
        )
        existing_ids = {doc["landmarkId"] for doc in existing_ids}
        landmark_ids = [lmid for lmid in landmark_ids if lmid not in existing_ids]

    if not landmark_ids:
        return jsonify({
            "status": "ok",
            "generated": 0,
            "skipped": "all exist" if not override else 0,
            "failed": 0
        })

    try:
        generator = LandmarkMetaGenerator("openai")
        generator.loadLandmarksFromDB(landmark_ids).fetchWiki().fetchOpenAI().storeToDB(collection_name="landmark_metadata", overwrite=False)

        return jsonify({
            "status": "ok",
            "generated": len(generator.landmarks),
            "skipped": len(data.get("landmarkIds", [])) - len(generator.landmarks),
            "failed": 0
        })
    except Exception as e:
        print(f"[Meta Generator] Error: {e}")
        return error_response("INTERNAL_ERROR", str(e), status=500)


if __name__ == "__main__":
    # app.run(port=5000)
    host = os.getenv('FLASK_HOST', '0.0.0.0')  # 默认值
    port = int(os.getenv('FLASK_PORT', '5000'))  # 默认值
    debug = os.getenv('FLASK_DEBUG', 'false').lower() == 'true'  # 默认值

    app.run(host=host, port=port, debug=debug)
