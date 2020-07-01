package com.liminal.eagamification.rewards;

public class RewardCategory {
    private int background_image;
    private int category_image;
    private String title;
    private String description;

    public RewardCategory(int background_image, int category_image, String title, String description) {
        this.background_image = background_image;
        this.category_image = category_image;
        this.title = title;
        this.description = description;
    }

    public int getBackground_image() {
        return background_image;
    }

    public int getCategory_image() {
        return category_image;
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
}
