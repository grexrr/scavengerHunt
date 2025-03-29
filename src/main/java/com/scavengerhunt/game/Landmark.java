package com.scavengerhunt.game;

/**
 * Represents a Landmark object.
 */
public class Landmark {
    private Integer id; 
    private String name;
    private String riddle;
    private double latitude;
    private double longitude;

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
    public Landmark(Integer id, String name, String riddle, double latitude, double longitude) {
        if (id == null || name == null || name.isEmpty()) {
            throw new IllegalArgumentException("id and name cannot be null or empty");
        }
        this.id = id;
        this.name = name;
        this.riddle = riddle;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Core
     */

    /**
     * Getters & Setters
     */
    public Integer getId() { return id; }

    public String getName() { return name; }

    public String getRiddle() { return riddle; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }
   
}
