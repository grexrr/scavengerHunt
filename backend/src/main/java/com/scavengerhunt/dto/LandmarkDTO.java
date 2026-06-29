package com.scavengerhunt.dto;

import java.util.List;

public class LandmarkDTO {
    private String id;
    private String name;

    private double latitude;
    private double longitude;
    private List<List<Double>> coordinates;

    public LandmarkDTO() {}

    public LandmarkDTO(String id, String name,  double lat, double lng, List<List<Double>> coordinates) {
        this.id = id;
        this.name = name;
        this.latitude = lat;
        this.longitude = lng;
        this.coordinates = coordinates;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public double getLatitude() { return this.latitude; }
    public double getLongitude() { return this.longitude; }

    public List<List<Double>> getCoordinates() { return coordinates; }
    public void setCoordinates(List<List<Double>> coordinates) { this.coordinates = coordinates; }
}
