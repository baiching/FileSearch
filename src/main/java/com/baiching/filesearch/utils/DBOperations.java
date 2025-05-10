package com.baiching.filesearch.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            Files.walkFileTree(startDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    insertPath(dir, pstmt);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    insertPath(file, pstmt);
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
