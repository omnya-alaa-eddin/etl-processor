package com.simon.etlProcessor.util;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import com.opencsv.bean.CsvToBeanBuilder;

public class CSVParser {

    public static List mapFileToObject(Reader fileReader, Class className) throws IOException {

        List output = new CsvToBeanBuilder(fileReader).withType(className).build().parse();
        output.forEach(System.out::println);
        output.remove(0);
        return output;

    }
}
