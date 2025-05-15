package com.baiching.filesearch;

import com.baiching.filesearch.utils.DBOperations;
import com.baiching.filesearch.utils.FileUtils;
import com.baiching.filesearch.utils.HotKeyManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class HelloApplication extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        setupUI();
    }

    public static void main(String[] args) throws IOException, SQLException {
        launch();
//        DBOperations db = new DBOperations();
//
//        System.out.println(db.searchPaths("ex2.R"));
    }

    private void setupUI() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("scanner.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Spotlight Clone");
        primaryStage.show();
    }

    @Override
    public void stop() {
        HotKeyManager.unregisterHotKey();
    }
}