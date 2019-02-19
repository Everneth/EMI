package com.everneth.emi.models;

public class LogPost {
    private int forum;
    private String title;
    private String post;
    private int author;
    public LogPost(int forum, String title, String post, int author)
    {
        this.forum = forum;
        this.title = title;
        this.post = post;
        this.author = author;
    }

    public int getForum() {
        return forum;
    }

    public void setForum(int forum) {
        this.forum = forum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }
    public int getAuthor() {
        return author;
    }

    public void setAuthor(int author) {
        this.author = author;
    }
}
