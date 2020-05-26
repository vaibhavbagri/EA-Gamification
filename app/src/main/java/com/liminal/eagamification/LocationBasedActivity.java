package com.liminal.eagamification;

public class LocationBasedActivity {
    public String name;
    public String description;
    public double latitude;
    public double longitude;
    public String assetBundleLink;
    public String markerLink;

    public LocationBasedActivity(String name, String description, double latitude, double longitude, String assetBundleLink, String markerLink){
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.assetBundleLink = assetBundleLink;
        this.markerLink = markerLink;
    }
}
