package com.real.matcher.model;

public enum MediaType {
    MOVIE("Movie"),
    TV_SEASON("TvSeries"),
    TV_EPISODE("TvEpisode");

    private final String type;

    MediaType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static MediaType fromString(String type) {
        for (MediaType mediaType : MediaType.values()) {
            if (mediaType.type.equalsIgnoreCase(type)) {
                return mediaType;
            }
        }
        return null;
    }
}
