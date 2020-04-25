package com.liminal.eagamification.rewards;

public class RewardDetails {
    public String rid;
    public String adminID;
    public String title;
    public String description;
    public long cost;
    public long quantity;

    RewardDetails(String rid, String adminID, String title, String description, long cost, long quantity){
        this.rid = rid;
        this.adminID = adminID;
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.quantity = quantity;
    }
}

