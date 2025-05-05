package com.baiching.filesearch;

import com.baiching.filesearch.utils.HotKeyManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        setupUI();
        //HotKeyManager.registerHotKey(this::toggleWindow);
    }

    public static void main(String[] args) {
        launch();
    }

    // Toggle window visibility (called when Ctrl+Space is pressed)
    private void toggleWindow() {
        if (primaryStage.isShowing()) {
            primaryStage.hide();
        }
        else {
            primaryStage.show();
            primaryStage.toFront(); // Bring to front
        }
    }

    private void setupUI() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("hello-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED); // Borderless window
        primaryStage.setTitle("Spotlight Clone");
        primaryStage.show(); // Start hidden (toggle via hotkey)
    }

    @Override
    public void stop() {
        HotKeyManager.unregisterHotKey();
    }
}