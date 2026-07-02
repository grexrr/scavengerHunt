import logging
import os
import json
from openai import OpenAI
from pymongo import MongoClient
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

logger = logging.getLogger(__name__)

class RiddleGenerator:
    def __init__(self, model=None, mongo_url=None) -> None:
        self.mongo_url = mongo_url or os.getenv('MONGO_URL')
        self.meta = {}
        self.mode = model.lower() if model else os.getenv('DEFAULT_MODEL')
        self.riddle = ""

        if self.mode == "local":
            import lmstudio as lms
            self.model = lms.llm(os.getenv('LMSTUDIO_MODEL'))
        elif self.mode == "chatgpt":
            api_key = os.getenv("OPENAI_API_KEY")
            if not api_key:
                raise ValueError("[RiddleGenerator] OPENAI_API_KEY not found in environment variables")
            self.model = OpenAI(api_key=api_key)
        else:
            raise ValueError(f"[RiddleGenerator] Unknown model mode: {self.mode}")

    def loadMetaFromDB(self, landmark_id):
        client = MongoClient(self.mongo_url)
        collection = client[os.getenv('MONGO_DATABASE')][os.getenv('MONGO_COLLECTION')]
        self.meta = collection.find_one({"landmarkId": landmark_id})
        if self.meta and "_id" in self.meta:
            self.meta["_id"] = str(self.meta["_id"])  # convert ObjectID to string
        return self

    def generateRiddle(self, language="English", style="medieval", difficulty=50, story_context=None):

        # ========== basic info collection ==========   
        meta = self.meta.get("meta", {})
        description = meta.get("description", {})

        # Check if description has any meaningful content
        history = description.get("history", []) if description else []
        architecture = description.get("architecture", []) if description else []
        significance = description.get("significance", []) if description else []
        
        # If no description data available, use basic landmark info (name, city) as fallback
        if not history and not architecture and not significance:
            landmark_name = self.meta.get("name", "landmark")
            city_name = self.meta.get("city", "city")
            logger.warning("No description data for %s in %s. Using basic info as fallback.", landmark_name, city_name)
            # Still proceed with generation using name and city info

        history_str = "history: " + ", ".join(history) if history else ""
        architecture_str = "architecture: " + ", ".join(architecture) if architecture else ""
        significance_str = "significance: " + ", ".join(significance) if significance else ""

        reference = ""
        if "wikipedia" in meta:
            reference = "reference:\n" + meta["wikipedia"]
        
        # Fallback: use basic landmark info if no description data
        basic_info = ""
        if not history_str and not architecture_str and not significance_str:
            landmark_name = self.meta.get("name", "")
            city_name = self.meta.get("city", "")
            if landmark_name and city_name:
                basic_info = f"landmark: {landmark_name}, city: {city_name}"
        
        # ========== prompt based on difficulty ==========   
        user_prompt = "\n".join(filter(None, [history_str, architecture_str, significance_str, reference, basic_info]))
        system_prompt = self._generateSystemPrompt(language, style, difficulty, story_context) 
        
        # ========== response based on model selection ==========   
        
        if self.mode == "local":
            # LMSTUDIO GUI STYLE
            ## Prompt format: https://www.llama.com/docs/model-cards-and-prompt-formats/meta-llama-3/
            template = r"""
            <|begin_of_text|><|start_header_id|>system<|end_header_id|>
            {system}
            <|eot_id|><|start_header_id|>user<|end_header_id|>
            {user}
            <|eot_id|><|start_header_id|>assistant<|end_header_id|>
            """

            final_prompt = template.format(system=system_prompt, user=user_prompt)

            result = self.model.respond(final_prompt)
            if hasattr(result, 'text'):
                self.riddle = result.text
            else:
                self.riddle = str(result)  # fallback to string conversion
            logger.debug("Generated riddle (local): %s", self.riddle)
            return self
        elif self.mode == "chatgpt":
            # chatGPT
            response = self.model.chat.completions.create(
                model="gpt-4-turbo",
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                temperature=0.5,
                max_tokens=500
            )
            self.riddle = response.choices[0].message.content.strip()
            logger.debug("Generated riddle (chatgpt): %s", self.riddle)
            return self
        else:
            raise ValueError(f"Unsupported mode: {self.mode}. Please choose either 'local' or 'chatgpt'.")
    
    def _generateSystemPrompt(self, language="English", style="Medieval", difficulty=50, story_context=None):
        
        # if isinstance(story_context, str) and story_context.strip():
        #     pass
        # else:
        #     context_prompt = " This is the opening of the story."

        try:
            difficulty = float(difficulty)
        except (ValueError, TypeError):
            difficulty = 50.0
            logger.warning("Invalid difficulty input, defaulting to 50.0")
        
        if difficulty < 33.3:
            diff_prompt = "Write a simple and clear riddle suitable for beginners or young audiences (around 10 years old)"
        elif difficulty < 66.6:
            diff_prompt = "Write a moderately challenging riddle with some use of rhetorical devices, but still solvable based on the context"
        else:
            diff_prompt = "Write a challenging and abstract riddle that relies on metaphor and indirect clues, avoiding clear landmark descriptions"

        

        system_prompt = f"""
            Written in {language}. You are a master riddle writer. {diff_prompt} 
            {story_context if story_context else "This is the opening of the story."}
            Do not include any extra explanations or mention the landmark's name explicitly.
            
            Use the following details as reference:
            \\begin{{quote}}
            \\textbf{{History}}: Highlight significant events or periods related to the landmark.
            \\textbf{{Architecture}}: Mention unique structural or design features, pay attention to color
            \\textbf{{Significance}}: Emphasize its cultural, religious, or social importance.
            \\textbf{{Length}}: No more than 5 lines.
            The riddle should be concise, engaging, and reflect a {style}.
            \\end{{quote}}

            Create a {style} riddle based on the information about {self.meta.get("name", "the landmark")} in {self.meta.get("city", "the city")}. 
            """
        return system_prompt

    # def saveToFile(self, filename):
    #     os.makedirs("outputfiles", exist_ok=True)
    #     path = os.path.join("outputfiles", filename)
    #     with open(path, 'w', encoding="utf-8") as f:
    #         json.dump(self.meta, f, ensure_ascii=False, indent=4)

if __name__ == "__main__":
    generator = RiddleGenerator()
    generator.loadMetaFromDB("6839d130b70cd905e96e359f").saveToFile("raw_meta.json")
    generator.generateRiddle()
    print(generator.riddle)