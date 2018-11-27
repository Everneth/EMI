package com.everneth.emi.models;

public class CommandPayload {
    private String command;
    private String[] params;
    public CommandPayload(String command, String[] params)
    {
        this.command = command;
        this.params = params;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for(String param : this.getParams())
        {
            sb.append(" " + param);
        }
        return this.getCommand() + sb.toString();
    }
}
