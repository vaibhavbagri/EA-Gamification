package com.liminal.eagamification;

public class LocationBasedActivity {
    public String category;
    public String name;
    public String description;
    public double latitude;
    public double longitude;
    public String assetBundleLink;
    public String markerLink;
    public String highscorer;
    public long highscore;

    public LocationBasedActivity(String category, String name, String description, double latitude, double longitude, String assetBundleLink, String markerLink, String highscorer, long highscore){
        this.category = category;
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.assetBundleLink = assetBundleLink;
        this.markerLink = markerLink;
        this.highscorer = highscorer;
        this.highscore = highscore;
    }
}
