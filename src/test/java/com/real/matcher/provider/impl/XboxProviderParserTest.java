package com.real.matcher.provider.impl;

import com.real.matcher.Matcher.CsvStream;
import com.real.matcher.model.Movie;
import com.real.matcher.util.CsvUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class XboxProviderParserTest {

    private XboxProviderParser parser;

    @BeforeEach
    void setUp() {
        parser = new XboxProviderParser();
    }

    @Test
    void testParseValidCsv() {
        CsvStream csvStream = new CsvStream("header", Stream.of(
                "Country,StudioNetwork,MediaId,Title,OriginalReleaseDate,MediaType,SeriesMediaId,SeasonMediaId" +
                        ",SeriesTitle,SeasonNumber,EpisodeNumber,LicenseType,ActualRetailPrice,OfferStartDate," +
                        "OfferEndDate,Actors,Director,XboxLiveURL",
                "UNITED STATES,Troma Films,5fe4a35c-00fe-4a37-a746-b3d44f495025,Rowdy Girls,4/25/2000 12:00:00 AM,Movie" +
                        ",,,,,,EST SD,5.99,5/29/2013 4:01:00 AM,1/1/3000 12:00:00 AM,Shannon Tweed," +
                        "Steven Nevius,https://video.xbox.com/movie/5fe4a35c-00fe-4a37-a746-b3d44f495025",
                "UNITED STATES,Magnolia Pictures,ad112a43-c864-4e56-960d-872a4d66c227,Murder Party," +
                        "10/16/2007 12:00:00 AM,Movie,,,,,,EST SD,9.99,3/29/2012 4:01:00 AM,1/1/3000 12:00:00 AM," +
                        "Alex Barnett,Jeremy Saulnier,https://video.xbox.com/movie/ad112a43-c864-4e56-960d-872a4d66c227"
        ));

        try (MockedStatic<CsvUtil> csvUtilMockedStatic = mockStatic(CsvUtil.class)) {
            csvUtilMockedStatic.when(() -> CsvUtil.readAll(any(CsvStream.class)))
                    .thenReturn(csvStream.getDataRows()
                            .map(line -> line.split(","))
                            .collect(Collectors.toList()));

            List<Movie> movies = parser.parse(csvStream);

            assertEquals(2, movies.size());

            Movie movie1 = movies.get(0);
            assertEquals("5fe4a35c-00fe-4a37-a746-b3d44f495025", movie1.getExternalId());
            assertEquals("Rowdy Girls", movie1.getTitle());
            assertEquals(2000, movie1.getYear());
            assertEquals("steven nevius", movie1.getDirector());
            assertTrue(movie1.getActors().contains("shannon tweed"));

            Movie movie2 = movies.get(1);
            assertEquals("ad112a43-c864-4e56-960d-872a4d66c227", movie2.getExternalId());
            assertEquals("Murder Party", movie2.getTitle());
            assertEquals(2007, movie2.getYear());
            assertEquals("jeremy saulnier", movie2.getDirector());
            assertTrue(movie2.getActors().contains("alex barnett"));
        }
    }

    @Test
    void testParseInvalidCsv() {
        CsvStream csvStream = new CsvStream("header", Stream.of(
                "US,Universal,invalid_uuid,Furious 7,4/13/2015,Movie,,,,,,,," +
                        "Vin Diesel,Paul Walker,Jason Statham,James Wan,video.xbox.com"
        ));

        try (MockedStatic<CsvUtil> csvUtilMockedStatic = mockStatic(CsvUtil.class)) {
            csvUtilMockedStatic.when(() -> CsvUtil.readAll(any(CsvStream.class)))
                    .thenReturn(csvStream.getDataRows().map(line -> line.split(","))
                            .collect(Collectors.toList()));

            List<Movie> movies = parser.parse(csvStream);

            assertEquals(0, movies.size());
        }
    }

    @Test
    void testParseEmptyCsv() {
        CsvStream csvStream = new CsvStream("header", Stream.empty());

        try (MockedStatic<CsvUtil> csvUtilMockedStatic = mockStatic(CsvUtil.class)) {
            csvUtilMockedStatic.when(() -> CsvUtil.readAll(any(CsvStream.class)))
                    .thenReturn(csvStream.getDataRows().map(line -> line.split(","))
                            .collect(Collectors.toList()));

            List<Movie> movies = parser.parse(csvStream);

            assertEquals(0, movies.size());
        }
    }

    @Test
    void testParseNonMovieType() {
        CsvStream csvStream = new CsvStream("header", Stream.of(
                "US,Universal,531b964f-0cb9-4968-9b77-e547f2435225,Furious 7,4/13/2015,TV Show,,,,,,,," +
                        "Vin Diesel,Paul Walker,Jason Statham,James Wan,video.xbox.com"
        ));

        try (MockedStatic<CsvUtil> csvUtilMockedStatic = mockStatic(CsvUtil.class)) {
            csvUtilMockedStatic.when(() -> CsvUtil.readAll(any(CsvStream.class)))
                    .thenReturn(csvStream.getDataRows().map(line -> line.split(","))
                            .collect(Collectors.toList()));

            List<Movie> movies = parser.parse(csvStream);

            assertEquals(0, movies.size());
        }
    }

}
