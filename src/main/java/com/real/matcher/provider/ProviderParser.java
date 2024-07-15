package com.real.matcher.provider;

import com.real.matcher.Matcher;
import com.real.matcher.model.Movie;

import java.util.List;

public interface ProviderParser {
    List<Movie> parse(Matcher.CsvStream csvStream);
}