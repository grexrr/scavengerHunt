package com.scavengerhunt.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a Landmark object.
 */

 @Document(collection = "landmarks")
 public class Landmark {
     @Id
     private String id;
     
     private String name;
     private String city;
 
     private Map<String, Double> centroid = new HashMap<>();
     private GeoJsonPolygon geometry;
 
     private Double rating;
    //  private Double uncertainty;
     private LocalDateTime lastAnswered;
 
     private String riddle;
 
     public Landmark() {}
 
     public Landmark(String name, String city, Double latitude, Double longitude) {
         if (name == null || name.isEmpty()) {
             throw new IllegalArgumentException("id and name cannot be null or empty");
         }
         this.name = name;
         this.city = city;
         this.centroid.put("latitude", latitude);
         this.centroid.put("longitude", longitude);
     }
 
     // Getters and Setters
 
     public String getId() { return id; }
     public void setId(String id) { this.id = id; }
 
     public String getName() { return name; }
     public void setName(String name) { this.name = name; }
 
     public String getCity() { return city; }
     public void setCity(String city) { this.city = city; }
 
     public Map<String, Double> getCentroid() { return centroid; }
     public void setCentroid(Map<String, Double> centroid) { this.centroid = centroid; }
     public void setCentroid(Double lat, Double lng) {
         this.centroid.put("latitude", lat);
         this.centroid.put("longitude", lng);
     }
 
     public Double getLatitude() { return centroid.get("latitude"); }
     public Double getLongitude() { return centroid.get("longitude"); }
 
     public GeoJsonPolygon getGeometry() { return geometry; }
     public void setGeometry(GeoJsonPolygon geometry) { this.geometry = geometry; }
 
     public Double getRating() { return rating; }
     public void setRating(Double rating) { this.rating = rating; }
 
    //  public Double getUncertainty() { return uncertainty; }
    //  public void setUncertainty(Double uncertainty) { this.uncertainty = uncertainty; }
 
     public LocalDateTime getLastAnswered() { return lastAnswered; }
     public void setLastAnswered(LocalDateTime lastAnswered) { this.lastAnswered = lastAnswered; }
 
     public String getRiddle() { return riddle; }
     public void setRiddle(String riddle) { this.riddle = riddle; }
 }
 
