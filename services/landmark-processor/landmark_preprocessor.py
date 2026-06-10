import requests
from pymongo import MongoClient, GEOSPHERE
import json
import os

from landmark_meta_generator import LandmarkMetaGenerator
from dotenv import load_dotenv

load_dotenv()

class LandmarkPreprocessor:

    def __init__(self, query, city="Cork") -> None:
        self.query = query
        self.city = city
        self.osmUrl = "https://overpass-api.de/api/interpreter"
        self.rawFileName = "raw.json"
        self.rawData = None
        self.rawLandmarks = None
        self.processedLandmarks = None

    def fetchRaw(self):
        res = requests.post(
            self.osmUrl,
            data={"data": self.query},
            headers={
                "User-Agent": "scavengerHunt-landmark-processor/1.0 (your-email@example.com)",
                "Accept": "*/*",
            },
            timeout=180,
        )
        print(res.status_code)
        res.raise_for_status()
        self.rawData = res.text
        return self

    def findRawLandmarks(self, landmarks=None):
        if self.rawData:
            rawData = json.loads(self.rawData)
        else:
            raise ValueError("No raw data. Please run fetchRaw() first.")

        res = {}
        if not landmarks:
            for entry in rawData["elements"]:
                info = entry.get("tags", None)
                if info and "name" in info:
                    res[info["name"]] = entry
        else:
            for landmark in landmarks:
                for entry in rawData["elements"]:
                    info = entry.get("tags", None)
                    if info and "name" in info and info["name"] == landmark:
                        res[landmark] = entry
                        break
                else:
                    print(f"[Warn] {landmark} Not Found!")

        self.rawLandmarks = res
        return self

    def processRawLandmark(self):
        if not self.rawLandmarks:
            raise ValueError("No raw landmarks. Please run findRawLandmarks() first.")

        res = {}
        for name, info in self.rawLandmarks.items():
            package = {}

            # centroids
            centroid_lat = sum(pt["lat"] for pt in info["geometry"]) / len(info["geometry"])
            centroid_lon = sum(pt["lon"] for pt in info["geometry"]) / len(info["geometry"])

            package["latitude"] = centroid_lat
            package["longitude"] = centroid_lon

            # Keep all geometry points
            geometry_points = []
            for pt in info["geometry"]:
                geometry_points.append({
                    "lat": pt["lat"],
                    "lon": pt["lon"]
                })

            package["geometry"] = geometry_points

            info["tags"].pop("name", None)
            package["tags"] = info["tags"]
            res[name] = package

        self.processedLandmarks = res
        return self

    def storeToDB(self, overwrite=False, mongo_url="mongodb://localhost:27017", db_name="scavengerhunt"):
        if not self.processedLandmarks:
            raise ValueError("No processed landmaks. Run processRawLandmark() first")

        client = MongoClient(mongo_url)
        db = client[db_name]
        collection = db["landmarks"]

        inserted = 0
        skipped = 0

        for name, data in self.processedLandmarks.items():
            existing = collection.find_one({
                "name": name,
                "city": self.city
            })
            if existing and not overwrite:
                skipped += 1
                print(f"[→] Skipped (exists): {name}")
                continue

            doc = {
                "name": name,
                "city": self.city,
                "latitude": data["latitude"],
                "longitude": data["longitude"],
                "geometry": data["geometry"],
                "tags": data.get("tags", {}),
                "centroid": [data["latitude"], data["longitude"]],
                "rating": 0.5,
            }

            if existing and overwrite:
                collection.replace_one({"_id": existing["_id"]}, doc)
                print(f"[✓] Replaced: {name}")
            else:
                collection.insert_one(doc)
                print(f"[✓] Inserted: {name}")

            inserted += 1
        print(f"[Summary] landmarks/{self.city}: inserted={inserted}, skipped={skipped}")
        client.close()
        return self


    def saveAsFile(self, filename="processed.json"):
        if not self.processedLandmarks:
            raise ValueError("No processed landmarks to save. Run processRawLandmark() first.")

        os.makedirs("outputfiles", exist_ok=True)
        path = os.path.join("outputfiles", filename)

        with open(path, 'w', encoding='utf-8') as f:  # 添加 encoding='utf-8'
            json.dump(self.processedLandmarks, f, indent=2, ensure_ascii=False)  # 添加 ensure_ascii=False

        print(f"Processed landmarks saved to {path}")
        return self

    def saveRawOSMAsFile(self, filename="raw.json"):
        if not self.rawData:
            raise ValueError("No raw OSM data. Run fetchRaw() first.")

        os.makedirs("outputfiles", exist_ok=True)
        path = os.path.join("outputfiles", filename)

        with open(path, 'w') as f:
            f.write(self.rawData)

        print(f"[✓] Raw OSM data saved to {path}")
        return self


if __name__ == "__main__":
    query = """
    [out:json];
    area["name"="Cork"]["boundary"="administrative"]->.searchArea;

    (
        way["amenity"]["name"]["amenity"!="parking"]["amenity"!="parking_space"]["amenity"!="bicycle_parking"]["amenity"!="waste_disposal"](area.searchArea);
        way["tourism"]["name"]["tourism"!="guest_house"](area.searchArea);
        way["historic"]["name"](area.searchArea);
        way["leisure"]["name"]["leisure"!="pitch"](area.searchArea);
        way["building"]["name"](area.searchArea);
    );
    out geom;
    """

    query_landmarks = [
        "Glucksman Gallery",
        "Cork Greyhound Track",
        "Honan Collegiate Chapel",
        "the President's Garden",
        "Boole Library",
        "The Quad / Aula Maxima",
        "Brookfield Health Sciences Complex",
        "Western Gateway Building"
    ]

    pre = LandmarkPreprocessor(query).fetchRaw().findRawLandmarks(query_landmarks)

    full_raw = json.loads(pre.rawData)
    full_raw["elements"] = list(pre.rawLandmarks.values())
    with open("outputfiles/raw.json", "w", encoding="utf-8") as f:
        json.dump(full_raw, f, indent=2, ensure_ascii=False)
    print(f"saved {len(full_raw['elements'])} / {len(query_landmarks)} landmarks")
