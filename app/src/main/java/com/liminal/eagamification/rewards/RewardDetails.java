package com.liminal.eagamification.rewards;

public class RewardDetails {
    private String rid;
    private String adminID;
    private String title;
    private String description;
    private long cost;
    private long quantity;

    RewardDetails(String rid, String adminID, String title, String description, long cost, long quantity){
        this.rid = rid;
        this.adminID = adminID;
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.quantity = quantity;
    }

    public String getRid() {
        return rid;
    }

    public String getAdminID() {
        return adminID;
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

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public long getQuantity() {
        return quantity;
    }
}

