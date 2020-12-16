package com.example.mediaplayer;

public class Song {
    Integer number;
    String url, name, performer;

    public Song(Integer number, String url, String name, String performer) {
        this.number = number;
        this.url = url;
        this.name = name;
        this.performer = performer;
    }

    public Integer getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getPerformer() {
        return performer;
    }

    public String getUrl() {
        return url;
    }
}
