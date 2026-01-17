package org.automation.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TestUtils {

    static Path directory = Paths.get("tempFiles");
    /**
     * Writes a list of strings to a temporary file, one per line.
     * Returns the path to the file.
     */
    public static String writeListToTempFile(List<String> list,String id) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        Path tempFile = Files.createTempFile(directory,"testcasePaths-"+id, ".txt");
        Files.write(tempFile, list, StandardOpenOption.WRITE);
        tempFile.toFile().deleteOnExit(); // auto-delete on JVM exit
        return tempFile.toAbsolutePath().toString();
    }

    /**
     * Reads a file line by line into a List<String>.
     */
    public static List<String> readListFromFile(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }
}

