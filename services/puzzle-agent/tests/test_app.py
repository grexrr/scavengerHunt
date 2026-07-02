import pytest
from app import app

@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as c:
        yield c

def test_health_returns_200(client):
    resp = client.post("/health")
    assert resp.status_code == 200
    data = resp.get_json()
    assert data["status"] == "healthy"

def test_generate_riddle_missing_session_id_returns_400(client):
    resp = client.post("/generate-riddle", json = {
        "landmark_id": "lm1",
        "language": "English"
    })
    assert resp.status_code == 400
    assert "MISSING_SESSION_ID" == resp.get_json().get("code")

def test_generate_riddle_missing_landmark_id_returns_400(client):
    resp = client.post("/generate-riddle", json = {
        "session_id": "s1",
        "language": "English"
    })
    assert resp.status_code == 400
    assert "MISSING_LANDMARK_ID" == resp.get_json().get("code")

def test_reset_session_unknown_session_returns_404(client):
    resp = client.post("/reset-session", json = {"session_id": "nonexistent"})
    assert resp.status_code == 404
