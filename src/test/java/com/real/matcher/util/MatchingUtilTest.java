package com.real.matcher.util;

import com.real.matcher.model.Movie;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MatchingUtilTest {

    @Test
    public void testParseYear() {
        assertEquals(2020, MatchingUtil.parseYear("2020"));
        assertEquals(-1, MatchingUtil.parseYear("NULL"));
        assertEquals(-1, MatchingUtil.parseYear(null));
        assertEquals(-1, MatchingUtil.parseYear("invalidYear"));
    }

    @Test
    public void testParseYearFromDate() {
        assertEquals(2020, MatchingUtil.parseYearFromDate("01/01/2020 12:00"));
        assertEquals(-1, MatchingUtil.parseYearFromDate("NULL"));
        assertEquals(-1, MatchingUtil.parseYearFromDate(null));
        assertEquals(-1, MatchingUtil.parseYearFromDate("invalidDate"));
        assertEquals(2020, MatchingUtil.parseYearFromDate("01/01/2020"));
    }

    @Test
    public void testFindMatchingMovie() {
        // Prepare internal movies
        Set<String> actors1 = new HashSet<>(Arrays.asList("actor1", "actor2"));
        Movie movie1 = new Movie(1, "Test Movie", 2020, actors1, "director1");

        Set<String> actors2 = new HashSet<>(Arrays.asList("actor3", "actor4"));
        Movie movie2 = new Movie(2, "Test Movie", 2019, actors2, "director2");

        Map<String, List<Movie>> internalMoviesMap = new HashMap<>();
        internalMoviesMap.put("test movie", Arrays.asList(movie1, movie2));

        // External movie to match
        Set<String> externalActors = new HashSet<>(Arrays.asList("actor1", "actor2"));
        Movie externalMovie = new Movie(-1, "Test Movie", 2020, externalActors, "director1");

        Optional<Movie> match = MatchingUtil.findMatchingMovie(externalMovie, internalMoviesMap);
        assertTrue(match.isPresent());
        assertEquals(movie1.getId(), match.get().getId());

        // Test no match
        externalActors = new HashSet<>(List.of("actor5"));
        externalMovie = new Movie(-1, "Test Movie", 2020, externalActors, "director1");

        match = MatchingUtil.findMatchingMovie(externalMovie, internalMoviesMap);
        assertFalse(match.isPresent());
    }
}
