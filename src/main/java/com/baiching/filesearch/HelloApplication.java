package com.baiching.filesearch;

import com.baiching.filesearch.utils.FileUtils;
import com.baiching.filesearch.utils.HotKeyManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.skin.SliderSkin;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Set;

public class HelloApplication extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        //fileUtils.listAllFilesAndDirectories("E:/").forEach(files-> System.out.println(files));

        //setupUI();
        //HotKeyManager.registerHotKey(this::toggleWindow);
    }

    public static void main(String[] args) throws IOException {
        //launch();
        FileUtils fileUtils = new FileUtils();
        //Set<String> files = fileUtils.listAllFilesAndDirectories("C:\\Users\\bachm\\Downloads");
        Set<String> files = fileUtils.listAllFilesAndDirectories("D:\\");

        files.stream().limit(10).forEach(System.out::println);
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