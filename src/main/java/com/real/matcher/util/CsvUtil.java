package com.real.matcher.util;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.real.matcher.Matcher;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

public class CsvUtil {

    public static List<String[]> readAll(Matcher.CsvStream csvStream) throws IOException, CsvException {
        String data = csvStream.getHeaderRow() + "\n" + csvStream.getDataRows().collect(Collectors.joining("\n"));
        try (CSVReader reader = new CSVReader(new StringReader(data))) {
            return reader.readAll();
        }
    }
}