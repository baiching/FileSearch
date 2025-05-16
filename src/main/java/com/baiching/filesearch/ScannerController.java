package com.baiching.filesearch;

import com.baiching.filesearch.utils.DBOperations;
import com.gluonhq.charm.glisten.control.AutoCompleteTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ScannerController implements Initializable {
    @FXML
    private ComboBox<String> comboBox;

    @FXML
    private Button scanDrive;

    @FXML
    private AutoCompleteTextField<String> autoText;

    @FXML
    private ListView<String> lstView;

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
        //System.out.println(selectedItem);
    }
    @FXML
    public void scanButton(ActionEvent actionEvent) throws SQLException, IOException {
        String selectedItem = comboBox.getValue();
        DBOperations db = new DBOperations();
        db.writePathToDB(selectedItem);
    }

    public void autocompleteText(KeyEvent inputMethodEvent) {

        if (inputMethodEvent.getCode() == KeyCode.ENTER) {
            String input = autoText.getText().trim();
            if (!input.isEmpty()) {
                List<String> results;
                try {
                    results = db.searchPaths(autoText.getValue());
                    System.out.println(results);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                lstView.getItems().add(String.valueOf(results));
            }
        }

//        List<String> results;
//        try {
//            results = db.searchPaths(autoText.getValue());
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println(results);
//        ObservableList<String> list = FXCollections.observableArrayList(results);
//        lstView.setItems(list);
    }
}
