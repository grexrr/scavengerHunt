from flask import Flask, request, jsonify
# from riddle_generator import RiddleGenerator
from datetime import datetime
from story_weaver import StoryWeaver
import logging
import os
from dotenv import load_dotenv

import uuid

from typing import Tuple
from flask.wrappers import Response

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
logger = logging.getLogger(__name__)

def error_response(code: str, message:str, retryable: bool = False, status: int = 500) -> Tuple[Response, int]:
    return jsonify({
        "code": code,
        "message": message,
        "correlationId": str(uuid.uuid4()),
        "retryable": retryable
    }), status

# Load environment variables
load_dotenv()

app = Flask(__name__)

story_weaver = StoryWeaver()

@app.route("/generate-riddle", methods=["POST"])
def generate_riddle():
    data = request.get_json()

    session_id = data.get("session_id")        # backend not sent yet
    landmark_id = data.get("landmark_id")
    language = data.get("language", "English")
    style = data.get("style", "Medieval")
    difficulty = data.get("difficulty")
    puzzle_pool = data.get("puzzle_pool", [])  # backend not sent yet

    if not session_id:
        logger.warning("generate-riddle request missing session_id")
        return error_response("MISSING_SESSION_ID", "missing session id", status=400)

    if not landmark_id:
        return error_response("MISSING_LANDMARK_ID", "missing landmark id", status=400)

    try:
        story_weaver.start_episode(
            puzzle_pool=puzzle_pool,
            session_id=session_id
        )
    except ValueError as e:
        return error_response("START_EPISODE_ERROR", str(e), status=400)

    riddle = story_weaver.serve_riddle(
        language=language,
        style=style,
        difficulty=difficulty,
        landmark_id=landmark_id,
        session_id=session_id
    )

    if "error" in riddle:
        return error_response("RIDDLE_GENERATION_ERROR", riddle["error"], status=400)

    return jsonify({
        "status": "ok",
        "session_id": session_id,
        "landmark_id": landmark_id,
        "riddle": riddle["riddle"]
    })

@app.route("/reset-session", methods=["POST"])
def reset_session():
    data = request.get_json(force=True)
    sid = data.get("session_id")
    if not sid:
        return error_response("MISSING_SESSION_ID", "missing session id", status=400)

    if sid in story_weaver.sessions:
        story_weaver.sessions.pop(sid, None)
        return jsonify({"status": "ok", "message": f"Session {sid} reset"})
    else:
        return error_response("NOT_FOUND_SESSION_ID", f"Session {sid} not found", status=404)

@app.route("/health", methods=["POST"])
def isHealthy():
    try:
        if story_weaver is None:
            return error_response("UNHEALTHY", "StoryWeaver not initialized", status=500)

        return jsonify({
            "status": "healthy",
            "timestamp": datetime.now().isoformat() + "Z",
            "version": "1.0.0"
        }), 200

    except Exception as e:
        return error_response("UNHEALTHY", str(e), status=500)


if __name__ == "__main__":
    app.run(host=os.getenv('FLASK_HOST'), port=int(os.getenv('FLASK_PORT')), debug=os.getenv('FLASK_DEBUG') == 'true')
