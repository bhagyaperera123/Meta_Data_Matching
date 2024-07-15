package com.real.matcher.provider.impl;

import com.opencsv.exceptions.CsvException;
import com.real.matcher.Matcher;
import com.real.matcher.model.MediaType;
import com.real.matcher.model.Movie;
import com.real.matcher.provider.ProviderParser;
import com.real.matcher.util.CsvUtil;
import com.real.matcher.util.MatchingUtil;
import com.real.matcher.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.real.matcher.config.Constants.*;

public class XboxProviderParser implements ProviderParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(XboxProviderParser.class);
    private static final String EMPTY_CSV_WARNING = "The CSV file is empty";

    @Override
    public List<Movie> parse(Matcher.CsvStream externalCsvStream) {
        List<Movie> movies = new ArrayList<>();
        try {
            List<String[]> lines = CsvUtil.readAll(externalCsvStream);
            if (lines.isEmpty()) {
                LOGGER.warn(EMPTY_CSV_WARNING);
                return movies;
            }

            lines.remove(0); // Remove header

            for (String[] line : lines) {
                try {
                    Optional<Movie> movie = processLine(line);
                    movie.ifPresent(movies::add);
                } catch (Exception e) {
                    LOGGER.error("Error processing line: {}", String.join(",", line), e);
                }
            }
        } catch (IOException | CsvException e) {
            LOGGER.error("Error reading or parsing CSV file", e);
            throw new RuntimeException("Error reading or parsing CSV file", e);
        }
        return movies;
    }

    private Optional<Movie> processLine(String[] line) {
        String externalId = line[XBOX_MEDIA_ID];
        if (!UUIDUtil.isValidUUID(externalId)) {
            LOGGER.warn("Invalid UUID: {}", externalId);
            return Optional.empty();
        }

        String title = line[XBOX_TITLE];
        int year = MatchingUtil.parseYearFromDate(line[XBOX_RELEASE_DATE]);
        String actors = line[XBOX_ACTORS];
        String director = line[XBOX_DIRECTOR];
        String mediaType = line[5];

        if (!mediaType.equalsIgnoreCase(MediaType.MOVIE.getType())) {
            return Optional.empty();
        }

        Set<String> actorSet = (actors == null || actors.isEmpty()) ? Collections.emptySet() :
                Arrays.stream(actors.split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());

        director = director != null ? director.trim().toLowerCase() : "";

        Movie movie = new Movie(externalId, title, year, actorSet, director);
        return Optional.of(movie);
    }
}
