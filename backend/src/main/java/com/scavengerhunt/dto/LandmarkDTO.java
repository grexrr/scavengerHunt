package com.scavengerhunt.dto;

import java.util.List;
import java.util.Map;

public class LandmarkDTO {
    private String id;
    private String name;
    private List<List<Double>> coordinates;
    private Map<String, Double> centroid;

    public LandmarkDTO() {}

    // public LandmarkDTO(String id, String name, List<List<Double>> coordinates) {
    //     this.id = id;
    //     this.name = name;
    //     this.coordinates = coordinates;
    // }

    public LandmarkDTO(String id, String name,  Map<String, Double> centroid, List<List<Double>> coordinates) {
        this.id = id;
        this.name = name;
        this.centroid = centroid;
        this.coordinates = coordinates;
    }

    // Getters & Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Double> getCentroid() {
        return centroid;
    }

    public void setCentroid(Map<String, Double> centroid) {
        this.centroid = centroid;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }
}
