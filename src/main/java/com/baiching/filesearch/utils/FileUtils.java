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
        Path startDir = Paths.get(dir);

        Files.walkFileTree(startDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                allPaths.add(dir.toAbsolutePath().toString());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                allPaths.add(file.toAbsolutePath().toString());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                if (exc instanceof AccessDeniedException) {
                    // Skip directories/files that trigger access denied
                    return FileVisitResult.SKIP_SUBTREE;
                }
                // Log other errors if needed
                return FileVisitResult.CONTINUE;
            }
        });

        return allPaths;
    }



}
