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
        res = requests.post(self.osmUrl, data={"data": self.query})
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

    def storeToDB(self, collection_name="landmark_metadata", overwrite=False):
        client = MongoClient(self.mongo_url)
        db = client[self.db_name]
        collection = db[collection_name]

        # 为landmarkId创建唯一索引
        try:
            collection.create_index("landmarkId", unique=True)
        except Exception:
            # 如果已有重复数据，索引创建会失败，需要先清理
            print("[!] Unique index creation failed, cleaning duplicates first...")
            pass

        inserted_count = 0
        skipped_count = 0
        cleaned_count = 0

        for lm_id, info in self.metaInfo.items():
            entry = {
                "landmarkId": lm_id,
                "name": info["name"],
                "city": info.get("city", ""),
                "meta": info.get("meta", {})
            }

            # 查找所有匹配的记录
            existing_docs = list(collection.find({"landmarkId": lm_id}))
            
            if existing_docs:
                if overwrite:
                    # 如果要覆盖，删除所有旧记录，插入新的
                    result = collection.delete_many({"landmarkId": lm_id})
                    cleaned_count += result.deleted_count
                    
                    collection.insert_one(entry)
                    inserted_count += 1
                    print(f"[✓] Replaced {result.deleted_count} old record(s) for: {info['name']}")
                else:
                    # 不覆盖的情况下，检查是否有完整的 description
                    has_complete_data = any(
                        doc.get("meta", {}).get("description") 
                        for doc in existing_docs
                    )
                    
                    if has_complete_data:
                        # 已有完整数据，跳过
                        skipped_count += 1
                        print(f"[→] Skipped (complete data exists): {info['name']}")
                    else:
                        # 已有数据不完整，删除并插入新的
                        result = collection.delete_many({"landmarkId": lm_id})
                        cleaned_count += result.deleted_count
                        
                        collection.insert_one(entry)
                        inserted_count += 1
                        print(f"[✓] Replaced {result.deleted_count} incomplete record(s) for: {info['name']}")
            else:
                # 不存在则插入新记录
                collection.insert_one(entry)
                inserted_count += 1
                print(f"[✓] Inserted: {info['name']}")

        print(f"\n[Summary] Collection: {collection_name}")
        print(f"  - Inserted: {inserted_count}")
        print(f"  - Skipped: {skipped_count}")
        print(f"  - Old Records Cleaned: {cleaned_count}")
        
        # 清理完后，确保创建唯一索引
        try:
            collection.create_index("landmarkId", unique=True)
            print(f"[✓] Unique index ensured on landmarkId")
        except Exception as e:
            print(f"[!] Could not create unique index: {e}")

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

    query = """
    [out:json][timeout:180];
    (
    way["amenity"]["name"]["amenity"!="parking"]["amenity"!="parking_space"]["amenity"!="bicycle_parking"]["amenity"!="waste_disposal"]
        (poly:"23.09998 113.31101 23.10002 113.32784 23.12860 113.32719 23.13010 113.31097");
    way["tourism"]["name"]["tourism"!="guest_house"]
        (poly:"23.09998 113.31101 23.10002 113.32784 23.12860 113.32719 23.13010 113.31097");
    way["historic"]["name"]
        (poly:"23.09998 113.31101 23.10002 113.32784 23.12860 113.32719 23.13010 113.31097");
    way["leisure"]["name"]["leisure"!="pitch"]
        (poly:"23.09998 113.31101 23.10002 113.32784 23.12860 113.32719 23.13010 113.31097");
    way["building"]["name"]
        (poly:"23.09998 113.31101 23.10002 113.32784 23.12860 113.32719 23.13010 113.31097");
    node["historic"]["indoor"!="yes"]
        (poly:"23.09998 113.31101 23.10002 113.32784 23.12860 113.32719 23.13010 113.31097");
    );
    out tags geom;
    """


    # query_landmarks = [
    #     "Glucksman Gallery",
    #     "Cork Greyhound Track",
    #     "Honan Collegiate Chapel",
    #     "the President's Garden",
    #     "Boole Library",
    #     "The Quad / Aula Maxima",
    #     "Brookfield Health Sciences Complex",
    #     "Western Gateway Building"
    # ]

    query_landmarks = [
        "广州市第二少年宫",
        "广州图书馆",
        "广东省博物馆",
        "广州国际金融中心(广州西塔)",
        "海心沙亚运公园",
        "广州塔",
    ]

    processed_landmarks = (
    LandmarkPreprocessor(query)
        .fetchRaw()
        .findRawLandmarks(query_landmarks)
        .processRawLandmark()
        .storeToDB()
        .saveAsFile("guangzhou.json")
        # .saveAsFile("pre-processed.json")
        # .saveRawOSMAsFile("raw.json")
    )

    print("\n[!] Start generating Guangzhou metadata...")

    load_dotenv(override=True)
    api_key = os.getenv('OPENAI_API_KEY')

    if not api_key:
        print("[x] OPENAI_API_KEY not found in environment variables!")
    else:
        meta_generator = LandmarkMetaGenerator(api_key)

        meta_generator.loadLandmarksFromDB()

        original_landmarks = meta_generator.landmarks.copy()
        meta_generator.landmarks = [
            (lm_id, lm_name, city) for lm_id, lm_name, city in original_landmarks
            if lm_name in query_landmarks
        ]

        print(f"[✓] Selected {len(meta_generator.landmarks)} Guangzhou Landmark for metadata generation")

        if len(meta_generator.landmarks) == 0:
            print("[!] Warning: No match landmark, please ensure landmars are stored in DB")
        else:

            meta_generator.fetchWiki().fetchOpenAI()
            meta_generator.saveToFile("guangzhou_metadata.json")
            meta_generator.storeToDB(collection_name="landmark_metadata", overwrite=False)

            print("[✓]Guangzhou landmark metadata generated!")
