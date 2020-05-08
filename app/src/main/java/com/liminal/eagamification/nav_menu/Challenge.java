package com.liminal.eagamification.nav_menu;

public class Challenge {
    public long progress;
    public String description;
    public String rewardPoints;
    public String game;
    public long target;
    public String stat;

    Challenge(long progress, String description, String rewardPoints, String game, long target, String stat){
        this.progress = progress;
        this.description = description;
        this.rewardPoints = rewardPoints;
        this.game = game;
        this.target = target;
        this.stat = stat;
    }
}
