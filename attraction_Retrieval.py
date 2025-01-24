import requests
import json

# Overpass API 的 URL
overpass_url = "https://overpass-api.de/api/interpreter"

# Overpass QL 查询语句
query = """
[out:json];
(
    node["tourism"="museum"](51.85,-8.55,52.00,-8.35);
    node["tourism"="hotel"](51.85,-8.55,52.00,-8.35);
);
out body;
"""

# 发送请求到 Overpass API
response = requests.get(overpass_url, params={'data': query})

# 检查响应状态
if response.status_code == 200:
    # 解析 JSON 数据
    data = response.json()

    # 保存数据到文件（可选）
    with open("json/museums_in_cork.json", "w", encoding="utf-8") as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

    # 打印每个博物馆的信息
    for element in data['elements']:
        name = element.get('tags', {}).get('name', 'Unnamed')
        lat = element['lat']
        lon = element['lon']
        print(f"Museum Name: {name}, Latitude: {lat}, Longitude: {lon}")
else:
    print(f"Error: {response.status_code} - {response.text}")
