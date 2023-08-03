package com.everneth.emi.models;

public class CommandResponse {
    private String playerName;
    private String commandRun;
    private String message;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getCommandRun() {
        return commandRun;
    }

    public void setCommandRun(String commandRun) {
        this.commandRun = commandRun;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
