package com.baiching.filesearch;

import com.baiching.filesearch.utils.DBOperations;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ScannerController implements Initializable {
    @FXML
    private ComboBox<String> comboBox;

    @FXML
    private Button scanDrive;

//    @FXML
//    public void selectDrive(ActionEvent actionEvent) {
//        MenuItem item = (MenuItem) actionEvent.getSource();
//        String driveName = (String) item.getUserData();
//        System.out.println(driveName);
//    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboBox.setItems(FXCollections.observableArrayList("C:\\", "D:\\", "E:\\"));
        comboBox.setOnAction(this::getComboBoxData);
        //scanDrive.setOnAction(this::scanButton);
    }

    public void getComboBoxData(ActionEvent event) {
        String selectedItem = comboBox.getValue();
        System.out.println(selectedItem);
    }
    @FXML
    public void scanButton(ActionEvent actionEvent) throws SQLException, IOException {
        String selectedItem = comboBox.getValue();
        System.out.println(selectedItem);
        DBOperations db = new DBOperations();
        db.writePathToDB(selectedItem);
    }
}
