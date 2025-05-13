package com.baiching.filesearch.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DBOperations {
    public void createDatabase() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS paths (path TEXT PRIMARY KEY)");
        }
    }

    public void writePathToDB(String dir) throws SQLException, IOException {
        Path startDir = Paths.get(dir);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT OR IGNORE INTO paths VALUES (?)")) {

            conn.setAutoCommit(false);
            final int batchSize = 1000;
            AtomicInteger counter = new AtomicInteger(0);

            Files.walkFileTree(startDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    try {
                        addToBatch(dir, pstmt, counter, batchSize);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(dir.toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        addToBatch(file, pstmt, counter, batchSize);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(dir.toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return (exc instanceof AccessDeniedException)
                            ? FileVisitResult.SKIP_SUBTREE
                            : FileVisitResult.CONTINUE;
                }
            });
        }
        System.out.println("Finished writing paths to database");
    }
    private void addToBatch(Path path, PreparedStatement ps, AtomicInteger counter, int batchSize) throws SQLException {
        ps.setString(1, path.toAbsolutePath().toString());
        ps.addBatch(); // Add to batch instead of executing immediately

        // Execute batch every `batchSize` entries
        if (counter.incrementAndGet() % batchSize == 0) {
            ps.executeBatch();
            ps.getConnection().commit();
        }
    }

    private void insertPath(Path path, PreparedStatement ps) {
        try {
            ps.setString(1, path.toAbsolutePath().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> searchPaths(String keyword) throws SQLException {
        List<String> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement("SELECT path FROM paths WHERE path LIKE ?")) {
            pstmt.setString(1, "%" + keyword +"%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(rs.getString("path"));
            }
        }

        return results;
    }
}
