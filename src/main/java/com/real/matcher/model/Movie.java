package com.real.matcher.model;

import java.util.Set;

public class Movie {
    private final int id;
    private final String title;
    private final int year;
    private final String externalId;
    private final Set<String> actors;
    private final String director;

    // Constructor for internal movie database
    public Movie(int id, String title, int year, Set<String> actors, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.externalId = null;
        this.actors = actors;
        this.director = director;
    }

    // Constructor for external provider feeds
    public Movie(String externalId, String title, int year, Set<String> actors, String director) {
        this.id = -1; // Indicating this is an external movie
        this.title = title;
        this.year = year;
        this.externalId = externalId;
        this.actors = actors;
        this.director = director;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getExternalId() {
        return externalId;
    }

    public Set<String> getActors() {
        return actors;
    }

    public String getDirector() {
        return director;
    }
}