package com.real.matcher.util;

import com.real.matcher.model.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MatchingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchingUtil.class);

    public static int parseYear(String year) {
        try {
            if (year == null || year.equalsIgnoreCase("NULL")) {
                return -1;
            }
            return Integer.parseInt(year);
        } catch (NumberFormatException e) {
            LOGGER.error("Failed to parse year: {}. Returning -1.", year);
            return -1;
        }
    }

    public static int parseYearFromDate(String date) {
        try {
            if (date == null || date.equalsIgnoreCase("NULL")) {
                return -1;
            }
            String[] parts = date.split(" ")[0].split("/");
            return Integer.parseInt(parts[2]);
        } catch (Exception e) {
            LOGGER.error("Failed to parse year from date: {}. Returning -1.", date);
            return -1;
        }
    }

    public static Optional<Movie> findMatchingMovie(Movie externalMovie, Map<String, List<Movie>> internalMoviesMap) {
        String title = externalMovie.getTitle() != null ? externalMovie.getTitle().trim().toLowerCase() : "";
        String director = externalMovie.getDirector() != null ? externalMovie.getDirector().trim().toLowerCase() : "";

        List<Movie> potentialMatches = internalMoviesMap.getOrDefault(title, Collections.emptyList());

        Optional<Movie> matchingMovie = potentialMatches.stream()
                .filter(movie -> (externalMovie.getYear() == -1 || movie.getYear() == externalMovie.getYear()))
                .filter(movie -> {
                    boolean directorMatches = director.isEmpty()
                            || movie.getDirector().equalsIgnoreCase(director);

                    if (!directorMatches) {
                        return false;
                    }

                    return externalMovie.getActors().isEmpty()
                            || movie.getActors().containsAll(externalMovie.getActors());
                })
                .findFirst();

        if (matchingMovie.isPresent()) {
            LOGGER.debug("Found matching movie: {}", matchingMovie.get().getTitle());
        } else {
            LOGGER.debug("No matching movie found for external movie: title='{}', director='{}', year={}",
                    externalMovie.getTitle(), externalMovie.getDirector(), externalMovie.getYear());
        }

        return matchingMovie;
    }
}