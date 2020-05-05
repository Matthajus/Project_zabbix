package sk.upjs.zabbix;

import io.github.hengyunabc.zabbix.api.ZabbixApi;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application {

    public static ZabbixApi zabbixApi;

    public static Stage stage;

    public void start(Stage primaryStage) throws Exception {

        stage = primaryStage;
        ZabbixLoginController controller = new ZabbixLoginController();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("zabbixlogin.fxml"));
        fxmlLoader.setController(controller);
        Parent parent = fxmlLoader.load();
        Scene scene = new Scene(parent);
        stage.setTitle("Zabbix");
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);

    }

    public static void main(String[] args){
        launch(args);
    }

//    public static void before(String url) {
//        sk.upjs.zabbix.App.zabbixApi = new DefaultZabbixApi(url);
//        sk.upjs.zabbix.App.zabbixApi.init();
//    }
//
//    public static boolean testLogin(String userName, String userPassword) {
//        return sk.upjs.zabbix.App.zabbixApi.login(userName, userPassword);
//    }
//
//    public static String vytvorMapu(String mapName, int height, int width) {
//        return sk.upjs.zabbix.App.zabbixApi.createMap(mapName, height, width);
//    }

}