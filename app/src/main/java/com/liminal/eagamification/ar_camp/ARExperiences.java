package com.liminal.eagamification.ar_camp;

public class ARExperiences {
    public String arxid;
    public String title;
    public String description;
    public String rewards;

    ARExperiences(String arxid, String title, String description, String rewards){
        this.arxid = arxid;
        this.title = title;
        this.description = description;
        this.rewards = rewards;
    }
}
