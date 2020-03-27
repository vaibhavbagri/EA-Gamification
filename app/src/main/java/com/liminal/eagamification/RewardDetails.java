package com.liminal.eagamification;

class RewardDetails {
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

    String getRid(){
        return rid;
    }

    String getAdminID() {
        return adminID;
    }

    String getTitle() {
        return title;
    }

    String getDescription() {
        return description;
    }

    long getCost(){
        return cost;
    }

    long getQuantity() {
        return quantity;
    }
}

