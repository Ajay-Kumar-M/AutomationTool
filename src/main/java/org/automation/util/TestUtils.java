package org.automation.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TestUtils {

//    static Path directory = Paths.get("tempFiles");

    public static String writeListToTempFile(List<String> list,String id) throws IOException {
        Path directory = Paths.get(getTempDir());
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        Path tempFile = Files.createTempFile(directory,"testcasePaths-"+id, ".txt");
        Files.write(tempFile, list, StandardOpenOption.WRITE);
        tempFile.toFile().deleteOnExit(); // auto-delete on JVM exit
        return tempFile.toAbsolutePath().toString();
    }

    public static List<String> readListFromFile(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }

    public static String getTempDir() {
        String relativePath = "tempFolder";
        return new File(getTomcatRoot(), relativePath).getAbsolutePath();
    }

    public static String getResultDir() {
        String relativePath = "result";
        return new File(getTomcatRoot(), relativePath).getAbsolutePath();
    }

    public static String getTomcatRoot() {
        return System.getProperty("catalina.base");
    }

    public static String getWebappsDir() {
        return getTomcatRoot() + File.separator + "webapps";
    }

    public static String getAppDir(String appName) {
        return getWebappsDir() + File.separator + appName;
    }

    public static String getExternalDir(String relativePath) {
        String tomcatRoot = getTomcatRoot();
        return tomcatRoot != null ?
                new File(tomcatRoot, relativePath).getAbsolutePath() : null;
    }

    public static String getConfigDir() {
        return getWebappsDir() + File.separator + "AutomationTool/src/main/java/org/automation/config";
    }
}

