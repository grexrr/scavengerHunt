import json

# 加载 Overpass API 返回的 JSON 数据
with open("json/museums_in_cork.json", "r", encoding="utf-8") as f:
    overpass_data = json.load(f)

# 转换为 GeoJSON 格式
geojson_data = {
    "type": "FeatureCollection",
    "features": []
}

for element in overpass_data['elements']:
    # 仅处理有坐标的点
    if element['type'] == 'node':
        feature = {
            "type": "Feature",
            "geometry": {
                "type": "Point",
                "coordinates": [element['lon'], element['lat']]
            },
            "properties": element.get('tags', {})  # 将所有标签作为属性
        }
        geojson_data["features"].append(feature)

# 保存为 GeoJSON 文件
with open("output.geojson", "w", encoding="utf-8") as f:
    json.dump(geojson_data, f, indent=4, ensure_ascii=False)

print("GeoJSON file created: output.geojson")
