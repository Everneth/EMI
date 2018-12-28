package com.everneth.emi.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;

/**
 *     Class: FileUtils
 *     Author: Faceman (@TptMike)
 *     Purpose: Utility class to read in a file as a string for JSON (de)serialization
 *
 */

public class FileUtils {
    public static String readFileAsString(String path) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        Stream<String> dataIn = reader.lines();
        StringBuilder sb = new StringBuilder();
        dataIn.forEach(sb::append);
        reader.close();
        return sb.toString();
    }
}
