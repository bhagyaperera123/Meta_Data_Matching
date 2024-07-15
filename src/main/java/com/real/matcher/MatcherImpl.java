package com.real.matcher;

import com.opencsv.exceptions.CsvException;
import com.real.matcher.model.Movie;
import com.real.matcher.model.Person;
import com.real.matcher.provider.ProviderParser;
import com.real.matcher.provider.impl.XboxProviderParser;
import com.real.matcher.provider.impl.GooglePlayProviderParser;
import com.real.matcher.provider.impl.VuduProviderParser;
import com.real.matcher.provider.impl.AmazonInstantProviderParser;
import com.real.matcher.util.CsvUtil;
import com.real.matcher.util.MatchingUtil;
import com.real.matcher.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.real.matcher.config.Constants.*;

public class MatcherImpl implements Matcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherImpl.class);

    private final Map<Integer, List<Person>> moviePersonsMap;
    private final Map<String, List<Movie>> internalMoviesMap;
    private final Map<DatabaseType, ProviderParser> providerParsers;

    public MatcherImpl(CsvStream movieDb, CsvStream actorAndDirectorDb) {
        LOGGER.info("Importing database");
        this.moviePersonsMap = new HashMap<>();
        this.internalMoviesMap = new HashMap<>();
        this.providerParsers = new HashMap<>();

        // Initialize provider parsers
        providerParsers.put(DatabaseType.XBOX, new XboxProviderParser());
        providerParsers.put(DatabaseType.GOOGLE_PLAY, new GooglePlayProviderParser());
        providerParsers.put(DatabaseType.VUDU, new VuduProviderParser());
        providerParsers.put(DatabaseType.AMAZON_INSTANT, new AmazonInstantProviderParser());

        try {
            loadActorsAndDirectors(actorAndDirectorDb);
            loadMovies(movieDb);
        } catch (IOException | CsvException e) {
            LOGGER.error("Error loading databases", e);
        }

        LOGGER.info("Database imported");
    }

    private void loadMovies(CsvStream movieDb) throws IOException, CsvException {
        List<String[]> lines = CsvUtil.readAll(movieDb);
        LOGGER.info("Loaded {} movies", lines.size() - 1);

        // Skip header row
        lines.remove(0);

        for (String[] line : lines) {
            try {
                int id = Integer.parseInt(line[MOVIE_ID]);
                String title = line[MOVIE_TITLE].trim().toLowerCase();
                int year = MatchingUtil.parseYear(line[YEAR]);

                List<Person> persons = moviePersonsMap.get(id);
                if (persons != null && !persons.isEmpty()) {
                    Set<String> movieActors = persons.stream()
                            .filter(p -> STRING_CAST.equalsIgnoreCase(p.getRole()))
                            .map(p -> p.getName().trim().toLowerCase())
                            .collect(Collectors.toSet());

                    String movieDirector = persons.stream()
                            .filter(p -> STRING_DIRECTOR.equalsIgnoreCase(p.getRole()))
                            .map(p -> p.getName().trim().toLowerCase())
                            .findFirst()
                            .orElse("");

                    Movie movie = new Movie(id, title, year, movieActors, movieDirector);
                    internalMoviesMap.computeIfAbsent(title, k -> new ArrayList<>()).add(movie);
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Skipping invalid movie ID: {}", line[MOVIE_ID], e);
            } catch (Exception e) {
                LOGGER.error("Error processing movie: {}", Arrays.toString(line), e);
            }
        }
    }

    private void loadActorsAndDirectors(CsvStream actorAndDirectorDb) throws IOException, CsvException {
        List<String[]> lines = CsvUtil.readAll(actorAndDirectorDb);
        LOGGER.info("Loaded {} actor/director entries", lines.size() - 1);
        // Skip header row
        lines.remove(0);
        for (String[] line : lines) {
            int movieId = Integer.parseInt(line[ID]);
            String name = line[NAME];
            String role = line[ROLE];
            moviePersonsMap.computeIfAbsent(movieId, k -> new ArrayList<>()).add(new Person(name.trim(), role.trim()));
        }
    }

    @Override
    public List<IdMapping> match(DatabaseType databaseType, CsvStream externalDb) {
        ProviderParser providerParser = providerParsers.get(databaseType);
        if (providerParser == null) {
            throw new IllegalArgumentException("No parser found for database type: " + databaseType);
        }

        List<Movie> externalMovies = providerParser.parse(externalDb);
        List<IdMapping> idMappings = Collections.synchronizedList(new ArrayList<>());
        Set<String> seenExternalIds = ConcurrentHashMap.newKeySet();

        externalMovies.parallelStream().forEach(movie -> processMovie(movie, seenExternalIds, idMappings));

        return idMappings;
    }

    private void processMovie(Movie externalMovie, Set<String> seenExternalIds, List<IdMapping> idMappings) {
        String externalId = externalMovie.getExternalId();
        if (!UUIDUtil.isValidUUID(externalId)) {
            LOGGER.warn("Invalid external ID: {}", externalId);
            return;
        }

        if (!seenExternalIds.add(externalId)) {
            LOGGER.debug("Duplicate external ID: {}", externalId);
            return;
        }

        Optional<Movie> match = MatchingUtil.findMatchingMovie(externalMovie, internalMoviesMap);

        if (match.isPresent()) {
            Movie movie = match.get();
            idMappings.add(new IdMapping(movie.getId(), externalId));
        } else {
            LOGGER.debug("No match found for: {} ({})", externalMovie.getTitle(), externalMovie.getYear());
        }
    }
}