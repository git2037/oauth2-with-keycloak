package com.example.resourceserver.util;

import java.io.File;

public class FileUtils {
    public static String getPathFromName(String name, String location) {
        return location + File.separatorChar + name;
    }

    public static String getNameFromPath(String path) {
        return (String) path.subSequence(path.lastIndexOf(File.separatorChar) + 1, path.length());
    }
}
