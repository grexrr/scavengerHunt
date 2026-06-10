import json
from unittest.mock import MagicMock, patch

from landmark_preprocessor import LandmarkPreprocessor

# Minimal Overpass-shaped fixture: one named landmark + one unnamed (skipped).
RAW_OSM_DATA = {
    "version": 0.6,
    "generator": "test",
    "elements": [
        {
            "type": "way",
            "id": 1,
            "geometry": [
                {"lat": 51.895, "lon": -8.485},
                {"lat": 51.895, "lon": -8.484},
                {"lat": 51.894, "lon": -8.484},
                {"lat": 51.894, "lon": -8.485},
            ],
            "tags": {"name": "Test Hall", "building": "yes"},
        },
        {
            "type": "way",
            "id": 2,
            "geometry": [
                {"lat": 51.890, "lon": -8.490},
                {"lat": 51.891, "lon": -8.490},
                {"lat": 51.891, "lon": -8.489},
            ],
            "tags": {"building": "yes"},
        },
    ],
}

def make_preprocessor():
    p = LandmarkPreprocessor(query="dummy", city="TestCity")
    p.rawData = json.dumps(RAW_OSM_DATA)
    return p


def test_find_raw_landmarks_extracts_named_elements():
    p = make_preprocessor()
    p.findRawLandmarks()
    assert "Test Hall" in p.rawLandmarks
    assert len(p.rawLandmarks) == 1  # unnamed element skipped


def test_process_raw_landmark_computes_centroid():
    p = make_preprocessor()
    p.findRawLandmarks()
    p.processRawLandmark()

    result = p.processedLandmarks["Test Hall"]
    assert abs(result["latitude"] - 51.895) < 0.001
    assert abs(result["longitude"] - (-8.485)) < 0.001


def test_process_raw_landmark_includes_geometry():
    p = make_preprocessor()
    p.findRawLandmarks()
    p.processRawLandmark()

    geometry = p.processedLandmarks["Test Hall"]["geometry"]
    assert len(geometry) == 4
    assert "lat" in geometry[0]
    assert "lon" in geometry[0]


def test_store_to_db_inserts_landmark(tmp_path):
    p = make_preprocessor()
    p.findRawLandmarks()
    p.processRawLandmark()

    mock_collection = MagicMock()
    mock_collection.find_one.return_value = None  # nothing exists yet

    mock_db = MagicMock()
    mock_db.__getitem__ = MagicMock(return_value=mock_collection)

    mock_client = MagicMock()
    mock_client.__getitem__ = MagicMock(return_value=mock_db)

    with patch("landmark_preprocessor.MongoClient", return_value=mock_client):
        p.storeToDB(mongo_url="mongodb://fake:27017", db_name="testdb")

    mock_collection.insert_one.assert_called_once()
    inserted_doc = mock_collection.insert_one.call_args[0][0]
    assert inserted_doc["name"] == "Test Hall"
    assert inserted_doc["city"] == "TestCity"


def test_store_to_db_skips_existing_landmark():
    p = make_preprocessor()
    p.findRawLandmarks()
    p.processRawLandmark()

    mock_collection = MagicMock()
    mock_collection.find_one.return_value = {"_id": "abc", "name": "Test Hall"}

    mock_db = MagicMock()
    mock_db.__getitem__ = MagicMock(return_value=mock_collection)
    mock_client = MagicMock()
    mock_client.__getitem__ = MagicMock(return_value=mock_db)

    with patch("landmark_preprocessor.MongoClient", return_value=mock_client):
        p.storeToDB(mongo_url="mongodb://fake:27017", db_name="testdb", overwrite=False)

    mock_collection.insert_one.assert_not_called()
