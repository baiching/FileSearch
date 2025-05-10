package com.baiching.filesearch.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
    /*
    * File path helpers
     */
    public Set<String> listFilesUsingFilesList(String dir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
    }

    public Set<String> listAllFilesAndDirectories(String dir) throws IOException {
        Set<String> allPaths = new HashSet<>();

        try (Stream<Path> stream = Files.walk(Paths.get(dir))) {
            allPaths = stream
                    .map(Path::toAbsolutePath) // Get absolute full path
                    .map(Path::toString) // Convert to string
                    .collect(Collectors.toSet());
        }

        return allPaths;
    }

}
