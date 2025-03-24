package com.scavengerhunt.game;

/**
 * Represents a Landmark object.
 */
public class Landmark {
    private String id; 
    private String name;
    private String riddle;
    private double latitude;
    private double longitude;
    private boolean isSolved;

    /**
     * Constructs a new Landmark object.
     *
     * @param id The unique identifier of the landmark, cannot be null
     * @param name The name of the landmark, cannot be null or an empty string
     * @param riddle The riddle of the landmark, can be null
     * @param latitude The latitude of the landmark, range is -90 to 90
     * @param longitude The longitude of the landmark, range is -180 to 180
     * @param isSolved Whether the landmark has been solved
     * @throws IllegalArgumentException If id or name is null or an empty string
     */
    public Landmark(String id, String name, String riddle, double latitude, double longitude, boolean isSolved) {
        if (id == null || id.isEmpty() || name == null || name.isEmpty()) {
            throw new IllegalArgumentException("id and name cannot be null or empty");
        }
        this.id = id;
        this.name = name;
        this.riddle = riddle;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isSolved = isSolved;
    }

    /**
     * Gets the unique identifier of the landmark.
     *
     * @return The unique identifier of the landmark
     */
    public String getId() { return id; }

    /**
     * Gets the name of the landmark.
     *
     * @return The name of the landmark
     */
    public String getName() { return name; }

    /**
     * Gets the riddle of the landmark.
     *
     * @return The riddle of the landmark
     */
    public String getRiddle() { return riddle; }

    /**
     * Gets the latitude of the landmark.
     *
     * @return The latitude of the landmark
     */
    public double getLatitude() { return latitude; }

    /**
     * Gets the longitude of the landmark.
     *
     * @return The longitude of the landmark
     */
    public double getLongitude() { return longitude; }

    /**
     * Checks if the landmark has been solved.
     *
     * @return True if the landmark is solved, false otherwise
     */
    public boolean isSolved() { return isSolved; }

    /**
     * Marks the landmark as solved.
     */
    public void markSolved() {
        this.isSolved = true;
    }
}
