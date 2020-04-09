package com.liminal.eagamification;

public class ARGame {
    public String name;
    public String description;
    public double latitude;
    public double longitude;
    public long rewardPoints;

    public ARGame(String name, String description, double latitude, double longitude, long rewardPoints){
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rewardPoints = rewardPoints;
    }
}
