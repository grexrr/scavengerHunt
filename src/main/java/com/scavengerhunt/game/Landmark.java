package com.scavengerhunt.game;

public class Landmark {
    private String id; 
    private String name;
    private String riddle;
    private double latitude;
    private double longitude;
    private boolean isSolved;

    public Landmark(String id, String name, String riddle, double latitude, double longitude, boolean isSolved) {
        this.id = id;
        this.name = name;
        this.riddle = riddle;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isSolved = isSolved;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public String getRiddle() { return riddle; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public boolean isSolved() { return isSolved; }

    public void markSolved() {
        this.isSolved = true;
    }
}
