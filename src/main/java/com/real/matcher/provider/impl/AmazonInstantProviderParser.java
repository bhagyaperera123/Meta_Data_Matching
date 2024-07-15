package com.real.matcher.provider.impl;

import com.real.matcher.Matcher;
import com.real.matcher.model.Movie;
import com.real.matcher.provider.ProviderParser;

import java.util.List;

public class AmazonInstantProviderParser implements ProviderParser {
    @Override
    public List<Movie> parse(Matcher.CsvStream csvStream) {
        return List.of();
    }
}