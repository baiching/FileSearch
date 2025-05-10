package com.baiching.filesearch.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:src/main/resources/database/mydatabase.db";

    private DatabaseConnection(){}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
