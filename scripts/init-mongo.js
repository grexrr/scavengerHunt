db = db.getSiblingDB("scavengerhunt");

// Users
db.users.createIndex({ username: 1 }, { unique: true })
db.users.createIndex({ username: 1 }, { unique: true });
db.users.createIndex({ email: 1 }, { unique: true, sparse: true });

// Landmarks
db.landmarks.createIndex({ location: "2dsphere" });
db.landmarks.createIndex({ city: 1 });

// Landmark metadata
db.landmark_metadata.createIndex({ landmarkId: 1 }, { unique: true });

// Game sessions — TTL index removes sessions after 2 hours of inactivity
db.game_sessions.createIndex({ lastUpdated: 1 }, { expireAfterSeconds: 7200 });
db.game_sessions.createIndex({ userId: 1 });

// Background jobs — matches @Document(collection = "background_jobs")
db.background_jobs.createIndex({ status: 1 });
db.background_jobs.createIndex({ idempotencyKey: 1 }, { unique: true, sparse: true });

print("Indexes created successfully.");
