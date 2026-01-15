package org.automation.util;

import java.util.ArrayList;
import java.util.List;

public final class JsonFileListHolder {

    private static final ThreadLocal<List<String>> FILES = new ThreadLocal<>();

    public static void setFiles(List<String> files) {
        FILES.set(new ArrayList<>(files));
    }

    public static List<String> getFiles() {
        return FILES.get();
    }

    public static void clear() {
        FILES.remove();
    }
}
