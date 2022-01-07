package com.everneth.emi.utils;

import com.everneth.emi.EMI;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        FileWriter fw = null;
        if(!file.exists())
        {
            file.createNewFile();
        }
        try {
            fw = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(fw);
            writer.write(content);
            writer.flush();
        }
        catch(Exception e)
        {
            EMI.getPlugin().getLogger().warning(e.getMessage());
        }
        finally
        {
            if(fw != null) {
                try {
                    fw.flush();
                    fw.close();
                }
                catch (IOException e) {
                    EMI.getPlugin().getLogger().warning(e.getMessage());
                }
            }
        }
        return file;
    }
}
