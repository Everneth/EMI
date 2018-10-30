package com.everneth.EMI.models;

public class Motd {
    private int id;
    private int playerId;
    private String message;
    private boolean isPublic;
    public String name;


    public Motd(int id, int playerId, String message)
    {
        this.id = id;
        this.playerId = playerId;
        this.message = message;
    }

    public Motd(int id, int playerId, String message, String name)
    {
        this.id = id;
        this.playerId = playerId;
        this.message = message;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
