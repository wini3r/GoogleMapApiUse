package com.example.wini3.googlemapapiuse.model;

public class PointLocation {

    private String id;
    private String name;
    private double lat;
    private double lon;
    private String comment;
    private String uuid;
    private boolean isSpeak;

    public boolean isSpeak() {
        return isSpeak;
    }

    public void setSpeak(boolean speak) {
        isSpeak = speak;
    }

    public String getId() { return id; }
    public void setId(String id) {  this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public PointLocation(String id, double lat, double lon, String comment) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.comment = comment;
    }

    public PointLocation() {
    }

    @Override
    public String toString() {
        return "PointLocation{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", comment='" + comment + '\'' +
                '}';
    }
}
