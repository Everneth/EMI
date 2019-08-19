package com.everneth.emi.models;

import java.util.UUID;

public class WhitelistApp {

    private String inGameName;
    private String location;
    private int age;
    private String friend;
    private String bannedElsewhere;
    private String lookingFor;
    private String loveHate;
    private String intro;
    private String secretWord;
    private long discordId;
    private UUID minecraftUuid;
    private int step;
    private boolean inProgress;
    private boolean holdForNextStep;

    public WhitelistApp() {this.setHoldForNextStep(false);}

    public WhitelistApp(String inGameName, String location, int age, String friend, String bannedElsewhere, String lookingFor, String loveHate,
                        String intro, String secretWord, long discordId, UUID minecraftUuid)
    {
        this.inGameName = inGameName;
        this.location = location;
        this.age = age;
        this.friend = friend;
        this.lookingFor = lookingFor;
        this.bannedElsewhere = bannedElsewhere;
        this.loveHate = loveHate;
        this.intro = intro;
        this.secretWord = secretWord;
        this.discordId = discordId;
        this.minecraftUuid = minecraftUuid;
    }

    public String getInGameName() {
        return inGameName;
    }

    public void setInGameName(String inGameName) {
        this.inGameName = inGameName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }

    public String getLookingFor() {
        return lookingFor;
    }

    public void setLookingFor(String lookingFor) {
        this.lookingFor = lookingFor;
    }

    public String getLoveHate() {
        return loveHate;
    }

    public void setLoveHate(String loveHate) {
        this.loveHate = loveHate;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getSecretWord() {
        return secretWord;
    }

    public void setSecretWord(String secretWord) {
        this.secretWord = secretWord;
    }

    public String getBannedElsewhere() {
        return bannedElsewhere;
    }

    public void setBannedElsewhere(String bannedElsewhere) {
        this.bannedElsewhere = bannedElsewhere;
    }

    public long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(long discordId) {
        this.discordId = discordId;
    }

    public UUID getMinecraftUuid() {
        return minecraftUuid;
    }

    public void setMinecraftUuid(UUID minecraftUuid) {
        this.minecraftUuid = minecraftUuid;
    }

    public int getStep()
    {
        return this.step;
    }

    public void setStep(int step)
    {
        this.step = step;
    }

    public boolean isInProgress()
    {
        return this.inProgress;
    }

    public void setInProgress(boolean inProgress)
    {
        this.inProgress = inProgress;
    }

    public boolean isHoldForNextStep() {
        return holdForNextStep;
    }

    public void setHoldForNextStep(boolean holdForNextStep) {
        this.holdForNextStep = holdForNextStep;
    }
}
