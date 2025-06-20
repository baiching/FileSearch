package com.baiching.filesearch;

import com.baiching.filesearch.search.LuceneIndexManager;
import com.baiching.filesearch.service.IndexSyncService;
import com.baiching.filesearch.utils.DBOperations;
import com.baiching.filesearch.utils.FileUtils;
import com.baiching.filesearch.utils.HotKeyManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class HelloApplication extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        setupUI();
    }

    public static void main(String[] args) throws IOException, SQLException {
        try {
            // Initialize dependencies
            DBOperations dbOps = new DBOperations();
            LuceneIndexManager indexManager = new LuceneIndexManager("src/main/resources/lucene");

            // Create sync service
            IndexSyncService syncService = new IndexSyncService(dbOps, indexManager);

            // Perform initial sync
            syncService.initialSync();

            // Start periodic sync every 10 minutes
            syncService.startPeriodicSync(10, TimeUnit.MINUTES);

        } catch (Exception e) {
            e.printStackTrace();
        }

        launch();
    }

    private void setupUI() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("scanner.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());

        primaryStage.setScene(scene);
        primaryStage.setTitle("File Search");
        primaryStage.show();
    }

    @Override
    public void stop() {
        HotKeyManager.unregisterHotKey();
    }
}