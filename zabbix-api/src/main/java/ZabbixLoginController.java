import io.github.hengyunabc.zabbix.api.DefaultZabbixApi;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ZabbixLoginController {

    @FXML
    private TextField urlTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private Button loginButton;

    @FXML
    void initialize() {


        loginButton.setOnAction(e -> {
            String url = urlTextField.getText();
            String userPassword = passwordPasswordField.getText();
            String loggedUser = usernameTextField.getText();

            before(url);
            if (testLogin(loggedUser, userPassword)) {
                openZabbixMainWindow();
            } else {
                new Alert(Alert.AlertType.WARNING, "Wrong input data!").showAndWait();
            }
        });
    }

    private void openZabbixMainWindow() {
        ZabbixMainController controller = new ZabbixMainController();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("zabbixmain.fxml"));
        fxmlLoader.setController(controller);
        try {
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent);
            App.stage.setScene(scene);
            App.stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void before(String url) {
        App.zabbixApi = new DefaultZabbixApi(url);
        App.zabbixApi.init();
    }

    public static boolean testLogin(String userName, String userPassword) {
        return App.zabbixApi.login(userName, userPassword);
    }

}
