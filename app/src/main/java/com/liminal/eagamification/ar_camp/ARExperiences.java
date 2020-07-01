package com.liminal.eagamification.ar_camp;

public class ARExperiences {
    private String title;
    private String description;
    private String rewards;
    private String assetBundleLink;

    ARExperiences(String title, String description, String rewards, String assetBundleLink){
        this.title = title;
        this.description = description;
        this.rewards = rewards;
        this.assetBundleLink = assetBundleLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRewards() {
        return rewards;
    }

    public void setRewards(String rewards) {
        this.rewards = rewards;
    }

    public String getAssetBundleLink() {
        return assetBundleLink;
    }
}
