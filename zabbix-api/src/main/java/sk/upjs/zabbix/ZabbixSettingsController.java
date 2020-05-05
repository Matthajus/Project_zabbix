package sk.upjs.zabbix;

import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ZabbixSettingsController {

    @FXML
    private Button saveButton;

    @FXML
    private TextField urlNameTextField;

    @FXML
    private TextField urlLinkTextField;

    @FXML
    private Button addUrlButton;

    @FXML
    private Button removeUrlButton;

    @FXML
    private ListView<String> urlListView;

    @FXML
    void initialize() {
        System.out.println("Otvorene nastavenia!");

        Map<String, String> ulrMap = new HashMap<>();

        addUrlButton.setOnAction(e -> {
            if (urlNameTextField.getText().equals("")) {
                new Alert(Alert.AlertType.WARNING, "You must enter a name!").showAndWait();
            }
            else if (urlLinkTextField.getText().equals("")) {
                new Alert(Alert.AlertType.WARNING, "You must enter a URL address!").showAndWait();
            } else if (!ulrMap.containsKey(urlNameTextField.getText())){
                ulrMap.put(urlNameTextField.getText(), urlLinkTextField.getText());
                urlListView.getItems().add(urlNameTextField.getText() + "   ->  " + urlLinkTextField.getText());
            } else {
                new Alert(Alert.AlertType.WARNING, "Element with this name already exists!").showAndWait();
            }
        });

        removeUrlButton.setOnAction(e -> {
            try {
                String selectedRow ="";
                selectedRow = urlListView.getSelectionModel().getSelectedItem();
                if (!selectedRow.equals("")) {
                    String[] array = selectedRow.split(" ", 2);
                    String nameFromRow = array[0];

                    ulrMap.remove(nameFromRow);
                    urlListView.getItems().remove(urlListView.getSelectionModel().getSelectedIndex());
                }
            } catch (NullPointerException exception) {
                new Alert(Alert.AlertType.INFORMATION, "You need to select line in table.").showAndWait();
            }

        });

        saveButton.setOnAction(e -> {
            if (urlListView.getItems().isEmpty()) {
                ZabbixMainController.settingsStage.getScene().getWindow().hide();
            } else {
                ZabbixMainController.mapWithURL = ulrMap;
                ZabbixMainController.settingsStage.getScene().getWindow().hide();
            }

        });

    }
}