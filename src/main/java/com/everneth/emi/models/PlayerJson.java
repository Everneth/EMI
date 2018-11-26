package com.everneth.emi.models;

import java.util.UUID;

public class PlayerJson {
    private UUID uuid;
    private String name;
    private double health;
    private int level;
    private long lastPlayed;
    private long firstPlayed;
    public PlayerJson()
    {
        this.uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.name = "Steve";
        this.health = 20;
        this.level = 0;
        this.lastPlayed = 0;
        this.firstPlayed = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(long lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public long getFirstPlayed() {
        return firstPlayed;
    }

    public void setFirstPlayed(long firstPlayed) {
        this.firstPlayed = firstPlayed;
    }
}
