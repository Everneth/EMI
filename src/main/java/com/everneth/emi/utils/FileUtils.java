package com.everneth.emi.utils;

import java.io.*;
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
    public static File writeFileFromString(String path, String content) throws IOException
    {
        File file = new File(path);

        if(!file.exists())
        {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        BufferedWriter writer = new BufferedWriter(fw);
        writer.write(content);
        fw.close();
        return file;
    }
}
