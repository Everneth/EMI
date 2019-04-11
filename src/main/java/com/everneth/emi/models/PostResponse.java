package com.everneth.emi.models;

public class PostResponse {
    private int statusCode;
    private String url;

    public PostResponse(int statusCode, String url)
    {
        this.statusCode = statusCode;
        this.url = url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
