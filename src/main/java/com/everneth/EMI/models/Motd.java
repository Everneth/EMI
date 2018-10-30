package com.everneth.EMI.models;

public class Motd {
    private int id;
    private int playerId;
    private String message;
    private boolean isPublic;
    public String name;


    public Motd(int id, int playerId, String message, boolean isPublic)
    {
        this.id = id;
        this.playerId = playerId;
        this.message = message;
        this.isPublic = isPublic;
    }

    public Motd(int id, int playerId, String message, String name, boolean isPublic)
    {
        this.id = id;
        this.playerId = playerId;
        this.message = message;
        this.isPublic = isPublic;
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

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}
