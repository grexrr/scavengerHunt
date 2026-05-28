from flask import Flask, request, jsonify
# from riddle_generator import RiddleGenerator
from datetime import datetime
from story_weaver import StoryWeaver
import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

app = Flask(__name__)

story_weaver = StoryWeaver()

@app.route("/generate-riddle", methods=["POST"])
def generate_riddle():
    data = request.get_json()
    
    session_id = data.get("sessionId")        # backend not sent yet
    landmark_id = data.get("landmarkId")
    language = data.get("language", "English")
    style = data.get("style", "Medieval")
    difficulty = data.get("difficulty")
    puzzle_pool = data.get("puzzlePool", [])  # backend not sent yet

    if not session_id:
        print("No session_id")
        return jsonify({"error": "MISSING_SESSION_ID"}), 400
    if not landmark_id:
        return jsonify({"error": "MISSING_LANDMARK_ID"}), 400
    
    try:
        story_weaver.start_episode(
            puzzle_pool=puzzle_pool, 
            session_id=session_id
        )
    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    
    # story_weaver.start_episode(
    #     puzzle_pool=puzzle_pool, 
    #     session_id=session_id
    # )

    riddle = story_weaver.serve_riddle(
        language=language,
        style=style,
        difficulty=difficulty,
        landmark_id=landmark_id,
        session_id=session_id
    )

    if "error" in riddle:
        return jsonify(riddle), 400

    return jsonify({
        "status": "ok",
        "session_id": session_id,
        "landmarkId": landmark_id,
        "riddle": riddle["riddle"]
    })

@app.route("/reset-session", methods=["POST"])
def reset_session():
    data = request.get_json(force=True)
    sid = data.get("session_id")
    if not sid:
        return jsonify({"status": "error", "message": "session_id required"}), 400

    if sid in story_weaver.sessions:
        story_weaver.sessions.pop(sid, None)
        return jsonify({"status": "ok", "message": f"Session {sid} reset"})
    else:
        return jsonify({"status": "error", "message": f"Session {sid} not found"}), 404
    
@app.route("/health", methods=["POST"])
def isHealthy():
    try:
        if story_weaver is None:
            return jsonify({"status": "unhealthy", "error": "StoryWeaver not initialized"}), 500
        
        return jsonify({
            "status": "healthy",
            "timestamp": datetime.now().isoformat() + "Z",
            "version": "1.0.0"  
        }), 200
        
    except Exception as e:
        return jsonify({
            "status": "unhealthy", 
            "error": str(e)
        }), 500
    

if __name__ == "__main__":
    app.run(host=os.getenv('FLASK_HOST'), port=int(os.getenv('FLASK_PORT')), debug=os.getenv('FLASK_DEBUG') == 'true')