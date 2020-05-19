package com.liminal.eagamification.nav_menu;

public class Challenge {
    public String challengeType;
    public long progress;
    public String description;
    public String rewardType;
    public long rewardPoints;
    public String activityName;
    public long target;
    public String stat;
    public boolean isClaimed;
    public long position;

    Challenge(String challengeType, long progress, String description, String rewardType, long rewardPoints, String activityName, long target, String stat, boolean isClaimed, long position){
        this.challengeType = challengeType;
        this.progress = progress;
        this.description = description;
        this.rewardType = rewardType;
        this.rewardPoints = rewardPoints;
        this.activityName = activityName;
        this.target = target;
        this.stat = stat;
        this.isClaimed = isClaimed;
        this.position = position;
    }
}
