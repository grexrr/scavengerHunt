#!/usr/bin/env python3
"""
使用现有的 LandmarkPreprocessor 类手动注入地标
"""

from landmark_preprocessor import LandmarkPreprocessor
from pymongo import MongoClient, GEOSPHERE
from dotenv import load_dotenv
import os
from landmark_meta_generator import LandmarkMetaGenerator

load_dotenv()

# 配置
WAY_ID = 182676960
CITY = "Mallow"
MONGO_URL = os.getenv("MONGO_URL", "mongodb://localhost:27017")
DB_NAME = os.getenv("MONGO_DB", "scavengerhunt")

# 构建查询特定 way 的 Overpass 查询
query = f"""
[out:json];
way({WAY_ID});
out geom;
"""

print(f"[*] 查询 way {WAY_ID}...")

# 使用你的 LandmarkPreprocessor 类处理
processor = LandmarkPreprocessor(query, city=CITY)
processor.fetchRaw()
processor.findRawLandmarks()
processor.processRawLandmark()

# 存储到 landmarks 集合（使用你代码中的处理逻辑）
if processor.processedLandmarks:
    client = MongoClient(MONGO_URL)
    db = client[DB_NAME]
    collection = db["landmarks"]
    
    # 创建地理索引（如果需要）
    try:
        collection.create_index([("geometry", GEOSPHERE)])
    except Exception:
        pass
    
    inserted_ids = []  # 保存插入的地标 ID
    
    for name, data in processor.processedLandmarks.items():
        # 构建标准格式：centroid 对象
        centroid = {
            "latitude": data["latitude"],
            "longitude": data["longitude"]
        }
        
        # 转换为 GeoJSON Polygon 格式（数据库标准格式）
        coordinates = [[pt["lon"], pt["lat"]] for pt in data["geometry"]]
        # 闭合多边形
        if coordinates[0] != coordinates[-1]:
            coordinates.append(coordinates[0])
        
        geometry_geojson = {
            "type": "Polygon",
            "coordinates": [coordinates]
        }
        
        entry = {
            "name": name,
            "city": CITY,
            "centroid": centroid,
            "geometry": geometry_geojson,
            "tags": data.get("tags", {}),
            "riddle": None
        }
        
        # 检查是否已存在
        existing = collection.find_one({"name": name, "city": CITY})
        if existing:
            print(f"[→] 已存在: {name} (城市: {CITY})")
            inserted_ids.append(str(existing["_id"]))  # 保存已存在的地标 ID
        else:
            result = collection.insert_one(entry)
            inserted_ids.append(str(result.inserted_id))  # 保存新插入的地标 ID
            print(f"[✓] 已插入: {name} (城市: {CITY})")
    
    client.close()  # 现在可以安全关闭了
    
    # 如果需要生成 metadata，使用 LandmarkMetaGenerator
    if inserted_ids:
        print("\n[*] 生成 metadata...")
        generator = LandmarkMetaGenerator()
        generator.loadLandmarksFromDB(inserted_ids)
        generator.fetchWiki().fetchOpenAI()
        generator.storeToDB(collection_name="landmark_metadata", overwrite=False)
        print("[✓] Metadata 生成完成!")
    
    print("\n[✓] 完成!")
else:
    print("[✗] 没有处理后的地标数据")