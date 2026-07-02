import requests
import json
import logging
import os
import re
import wikipedia

from openai import OpenAI

from pymongo import MongoClient
from dotenv import load_dotenv
from bson import ObjectId

load_dotenv()

logger = logging.getLogger(__name__)

class LandmarkMetaGenerator:
    def __init__(self, mode="openai"):
        self.api_key = os.getenv("OPENAI_API_KEY", "")
        self.mongo_url = os.getenv("MONGO_URL", "mongodb://localhost:27017")
        self.db_name = os.getenv("MONGO_DB", "scavengerhunt")
        self.mode = mode
        self.metaInfo = {}
        self.landmarks = []

    def loadLandmarksFromDB(self, landmark_ids=None):
        client = MongoClient(self.mongo_url)
        db = client[self.db_name]
        collection = db.landmarks

        query = {}
        if landmark_ids:
            object_ids = []
            for lid in landmark_ids:
                try:
                    object_ids.append(ObjectId(lid))
                except Exception:
                    logger.warning("Invalid ObjectId format skipped: %s", lid)
            query = {"_id": {"$in": object_ids}}

        docs = collection.find(query, {"_id": 1, "name": 1, "city": 1})
        self.landmarks = [(str(doc["_id"]), doc["name"], doc.get("city", "")) for doc in docs]

        if landmark_ids:
            logger.info("Loaded %d/%d requested landmarks from DB.", len(self.landmarks), len(landmark_ids))
        else:
            logger.info("Loaded %d landmarks from DB.", len(self.landmarks))
        return self

    def fetchWiki(self):
        for lm_id, lm, city in self.landmarks:
            if lm_id not in self.metaInfo:
                    self.metaInfo[lm_id] = {}
                    self.metaInfo[lm_id]["name"] = lm
                    self.metaInfo[lm_id]["city"] = city
            try:
                page = wikipedia.page(lm, auto_suggest=True)
                logger.debug("Processing Wiki page: %s", lm)

                if "meta" not in self.metaInfo[lm_id]:
                    self.metaInfo[lm_id]["meta"] = {}
                self.metaInfo[lm_id]["meta"]["url"] = page.url
                self.metaInfo[lm_id]["meta"]["summary"] = page.summary
                self.metaInfo[lm_id]["meta"]["images"] = [img for img in page.images if img.lower().endswith((".jpg", ".jpeg", ".png"))]

                ### APPLY AI INSPECTION
                if self._aiInsepection(lm, city, page.summary) == True:
                    # if wiki is found, replace summary with detail
                    self.metaInfo[lm_id]["meta"]["wikipedia"] = page.content
                    self.metaInfo[lm_id]["meta"].pop("summary")
                else:
                    # found wiki is falsed. remove from library
                    self.metaInfo[lm_id].pop("meta")

            except wikipedia.exceptions.DisambiguationError as e:
                logger.warning("%s disambiguation: %s", lm, e.options[:3])

            except wikipedia.exceptions.PageError:
                logger.warning("%s page not found.", lm)
        return self


    def fetchOpenAI(self):
        for lm_id in self.metaInfo:
            if "meta" not in self.metaInfo[lm_id]:
                self.metaInfo[lm_id]["meta"] = {}

            lm_name = self.metaInfo[lm_id]["name"]
            lm_city = self.metaInfo[lm_id]["city"]

            content = self.metaInfo[lm_id].get("meta", {}).get("wikipedia")
            image_urls = self.metaInfo[lm_id].get("meta", {}).get("images")

            if "meta" not in self.metaInfo[lm_id]:
                self.metaInfo[lm_id]["meta"] = {}

            desc = self.metaInfo[lm_id]["meta"].get("description")
            if desc is None or desc == {}:
                logger.debug("Description for %s not found, generating via AI.", lm_name)
                result = self._aiSummarizeLandmark(lm_name, lm_city, content, image_urls)
                self.metaInfo[lm_id]["meta"]["description"] = result.get("metadata", {})

        return self


    def _aiSummarizeLandmark(self, lm_name, lm_city, content=None, image_urls=None, retry_count=0):
        # generate something similiar to wikipedia?
        client = OpenAI(api_key=self.api_key)

        if not content:
            content = "None"

        image_urls = image_urls[:5] if image_urls else []

        prompt = f"""
        Provide structured information about a real-world landmark called "{lm_name}" located in "{lm_city}".
        Additional information: {content}

        history: Highlight significant events or periods related to the landmark.\
        architecture: Mention unique structural or design features, pay attention to color\
        significance: Emphasize its cultural, religious, or social importance.\
        Each in 5~10 keywords.

        Respond with expected JSON format:
        {{
        "history": [...],
        "architecture": [...],
        "significance": [...]
        }}


        Do not include any explanation or commentary.
        Respond ONLY with this JSON format. Do NOT explain.
        If unsure, reply exactly with this string: `status: unknown`
        """

        try:
            response = client.chat.completions.create(
                model="gpt-4-turbo",
                messages=[
                    # {"role": "system", "content": "You are a precise document verifier."},
                    {"role": "user",
                     "content":
                     [{"type": "text", "text": prompt}] +
                     [{"type": "image_url", "image_url": { "url": url, "detail": "high" }} for url in image_urls]
                     }
                ],
                temperature=0.5,
                max_tokens=500
            )

            text = response.choices[0].message.content
            text = re.sub(r"```(?:json)?", "", text).replace("```", "").strip()

            if "not recognized" in text.lower():
                logger.warning("GPT did not recognize: %s", lm_name)
                return {
                "source": "openai",
                "confidence": False,
                "message": "LLM could not confirm landmark identity."
            }
            try:
                metadata = json.loads(text)
                return {
                    "source": "openai",
                    "confidence": True,
                    "metadata": metadata
                }
            except json.JSONDecodeError:
                logger.warning("GPT output for %s could not be parsed as JSON: %s", lm_name, text)
                # 重试逻辑
                if retry_count < 1:
                    logger.warning("Retrying for %s (attempt %d)", lm_name, retry_count + 1)
                    return self._aiSummarizeLandmark(lm_name, lm_city, content, image_urls, retry_count + 1)
                return {
                    "source": "openai",
                    "confidence": False,
                    "message": "Malformed JSON returned by GPT."
                }
        except Exception as e:
            logger.error("Fallback GPT error for %s: %s", lm_name, e)
            # 重试逻辑
            if retry_count < 1:
                logger.warning("Retrying for %s (attempt %d)", lm_name, retry_count + 1)
                return self._aiSummarizeLandmark(lm_name, lm_city, content, image_urls, retry_count + 1)
            return {
                "source": "openai",
                "confidence": False,
                "message": "Error during fallback."
            }

    def _aiInsepection(self, lm_name, lm_city, content):
        client = OpenAI(api_key=self.api_key)

        prompt = f"""
        You are verifying if a Wikipedia article is about a specific landmark.
        Target Landmark: "{lm_name}"
        City: "{lm_city}"
        Text:
        \"\"\"
        {content}
        \"\"\"
        If this page is clearly about the target landmark, respond with **only** the word: `true`. Otherwise, respond with `false`.
        """

        try:
            response = client.chat.completions.create(
                model="gpt-4",
                messages=[
                    {"role": "system", "content": "You are a precise document verifier."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.2,
                max_tokens=5
            )
            reply = response.choices[0].message.content.strip().lower()
            return reply.startswith("true")

        except Exception as e:
            logger.error("GPT error during verification for %s: %s", lm_name, e)


    def saveToFile(self, filename="meta_output.json"):
        os.makedirs("outputfiles", exist_ok=True)
        path = os.path.join("outputfiles", filename)
        with open(path, 'w', encoding='utf-8') as f:
            json.dump(self.metaInfo, f, ensure_ascii=False, indent=4)

    def storeToDB(self, collection_name="landmark_metadata", overwrite=False):
        client = MongoClient(self.mongo_url)
        db = client[self.db_name]
        collection = db[collection_name]
        collection.create_index("landmarkId", unique=True)

        inserted_count = 0
        skipped_count = 0
        updated_count = 0

        for lm_id, info in self.metaInfo.items():
            entry = {
                "landmarkId": lm_id,
                "name": info["name"],
                "city": info.get("city", ""),
                "meta": info.get("meta", {})
            }

            # 检查数据库中是否已存在该地标
            existing = collection.find_one({"landmarkId": lm_id})

            if existing:
                if overwrite:
                    # 如果设置了overwrite，更新现有记录
                    collection.update_one(
                        {"landmarkId": lm_id},
                        {"$set": entry}
                    )
                    updated_count += 1
                    logger.debug("Updated: %s", info['name'])
                else:
                    # 如果已存在且不覆盖，跳过
                    skipped_count += 1
                    logger.debug("Skipped (already exists): %s", info['name'])
            else:
                # 不存在则插入新记录
                collection.insert_one(entry)
                inserted_count += 1
                logger.debug("Inserted: %s", info['name'])

        logger.info("Collection %s: inserted=%d, skipped=%d, updated=%d",
            collection_name, inserted_count, skipped_count, updated_count)


if __name__ == "__main__":

    generator = (
        LandmarkMetaGenerator()
        .loadLandmarksFromDB()
        .fetchWiki()
        .fetchOpenAI()
    )

    generator.saveToFile("final-meta.json")
    generator.storeToDB()
