package com.baiching.filesearch;

import com.baiching.filesearch.utils.DBOperations;
import com.gluonhq.charm.glisten.control.AutoCompleteTextField;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.ResourceBundle;

public class ScannerController implements Initializable {
    @FXML
    private ComboBox<String> comboBox;

    @FXML
    private Button scanDrive;

//    @FXML
//    private AutoCompleteTextField<String> autoText;

    private final DBOperations db = new DBOperations();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboBox.setItems(FXCollections.observableArrayList("C:\\", "D:\\", "E:\\"));
        comboBox.setOnAction(this::getComboBoxData);
        //comboBox.setOnAction(this::autocompleteText);
        //scanDrive.setOnAction(this::scanButton);

//        autoText.setCompleter(keyword -> {
//            try {
//                return db.searchPaths(keyword);
//            }
//            catch (SQLException e) {
//                e.printStackTrace();
//                return Collections.emptyList();
//            }
//        });

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

    public void autocompleteText(InputMethodEvent inputMethodEvent) {
    }
}
