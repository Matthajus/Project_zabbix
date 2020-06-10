package sk.upjs.zabbix;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ZabbixMainController {

    public static Stage settingsStage = new Stage();
    public static Map<String, String> mapWithURL = new HashMap<>();

    private String selectedHostGroup;
    private String selectedHostGroupId;

    private Map<String, String> hosts;

    private String selectedHost;
    private String selectedHostId;

    @FXML
    private ComboBox<String> hostGroupComboBox;

    @FXML
    private ComboBox<String> hostComboBox;

    @FXML
    private AnchorPane mapSettingsAnchorPane;

    @FXML
    private Button confirmButton;

    @FXML
    private TextField mapNameTextField;

    @FXML
    private TextField mapHeightTextField;

    @FXML
    private TextField mapWidthTextField;

    @FXML
    private Button createMapButton;

    @FXML
    private Button otherSettingsButton;

    @FXML
    void initialize() throws ParseException {
        hostComboBox.setDisable(true);
        confirmButton.setDisable(true);
        mapSettingsAnchorPane.setVisible(false);

        List<String> triggers = new ArrayList<>();
        List<String> triggersId = new ArrayList<>();
        final Map<String, String> hostGroups = parsujHostGroupy(vratHostGroupy());


        for (String key : hostGroups.keySet()) {
            hostGroupComboBox.getItems().add(key);
        }

        final boolean isHostGroupComboBoxEmpty = hostGroupComboBox.getSelectionModel().isEmpty();

        hostGroupComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isHostGroupComboBoxEmpty) {
                hostComboBox.setDisable(false);
                hostComboBox.getItems().clear();
                selectedHostGroup = hostGroupComboBox.getValue();
                Map<String, String> hosts = null;
                try {
                    hosts = parsujHostov(vratHostov(hostGroups.get(selectedHostGroup)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                hostGroups.forEach((key, value) -> {
                    if (key == selectedHostGroup) selectedHostGroupId = value;
                });
                System.out.println(selectedHostGroupId);

                assert hosts != null;
                for (String key : hosts.keySet()) {
                    hostComboBox.getItems().add(key);
                }

                this.hosts = hosts;
            }
        });

        final boolean isHostsComboBoxEmpty = hostComboBox.getSelectionModel().isEmpty();
        hostComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isHostsComboBoxEmpty) {
                confirmButton.setDisable(false);
            }
        });

        confirmButton.setOnAction(e -> {
            triggers.clear();
            triggersId.clear();

            selectedHost = hostComboBox.getSelectionModel().getSelectedItem();

            hosts.forEach((key, value) -> {
                if (key == selectedHost) selectedHostId = value;
            });
            System.out.println(selectedHostId);

            mapNameTextField.setText(selectedHost);

            Map<String, String> map = null;
            try {
                map = parsujTriggery(vratTriggery(selectedHost, "link down"));
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

            // sortujeme mapu, tak aby sme mali usporiadane triggery ako maju byt v poradi
            assert map != null;
            List<String> list = new ArrayList<>();
            map.forEach((key, value) -> {
                if (!(key.contains("Interface G") && key.contains("Link down")))
                    list.add(key);
            });

            map.forEach((key, value) -> System.out.println("Trigger: " + key + "    ID: " + value));

            for (String trigger : list) {
                map.remove(trigger);
            }

            map = sortByValue(map);
            map.forEach((key, values) -> {
                triggers.add(key);
                triggersId.add(values);
            });

            int i = 0;
            Collections.reverse(triggers);
            Collections.reverse(triggersId);
            for (String trigger : triggers) {
                System.out.println(trigger + triggersId.get(i));
                i++;
            } // koniec sortovania

            mapSettingsAnchorPane.setVisible(true);
        });


        otherSettingsButton.setOnAction(e -> {
            ZabbixSettingsController controller = new ZabbixSettingsController();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("zabbixsettings.fxml"));
            fxmlLoader.setController(controller);
            try {
                Parent parent = fxmlLoader.load();
                Scene scene = new Scene(parent);
                settingsStage.setTitle("Map settings");
                settingsStage.setScene(scene);
                settingsStage.show();
                settingsStage.setResizable(false);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        });

        // po klikuti na button vypise ze sme vytvorili mapu v pripade, ze s takym nazvom este nie je
        // ak uz mapa s takym nazvom existuje, vyhodi nam warning
        createMapButton.setOnAction(e -> {
            String mapName = mapNameTextField.getText();
            int mapHeight = Integer.parseInt(mapHeightTextField.getText());
            int mapWidth = Integer.parseInt(mapWidthTextField.getText());

            List<String> idGrafov = new ArrayList<>();

            try {
                HashMap<String, String> graphMap = (HashMap<String, String>) parsujGrafy(vratGrafy(selectedHost, "Gi1"));
                graphMap.forEach((key, value) -> idGrafov.add(value));
                Collections.sort(idGrafov);
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            }

            String createdMapJson = vytvorMapu(mapName, mapHeight, mapWidth, triggersId, idGrafov, mapWithURL, selectedHostGroupId, selectedHostId);

            if (createdMapJson == null)
                new Alert(Alert.AlertType.WARNING, "Map with that name already exists!").showAndWait();
            else {
                new Alert(Alert.AlertType.INFORMATION, "You created a new map.").showAndWait();
                System.out.println(createdMapJson);
                String mapID = createdMapJson.substring(15, createdMapJson.length() - 3);
                String url = "https://zabbix.csirt.upjs.sk/zabbix.php?action=map.view&sysmapid=" + mapID;
                try {
                    Desktop.getDesktop().browse(new URL(url).toURI());
                } catch (Exception ignored) {
                }
                mapWithURL = new HashMap<>();
            }
        });


    }


    public static Map<String, String> sortByValue(final Map<String, String> wordCounts) {
        return wordCounts.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    // metoda vytorí mapu
    public String vytvorMapu(String mapName, int height, int width, List<String> triggerID, List<String> graphID, Map<String, String> mapWithURL, String selectedGroupID, String selectedHostID) {
        return App.zabbixApi.createMap(mapName, height, width, triggerID, graphID, mapWithURL, selectedGroupID, selectedHostID);
    }

    // metoda vrati trigery, ktore patria zadanemu hostovi s prislusnym popisom
    // metoda vrati json format
    public String vratTriggery(String menoHosta, String description) {
        return "{ \"result\": " + App.zabbixApi.getTriggers(menoHosta, description) + " }";
    }

    // metoda vrati vsetky host groupy
    // metoda vrati json format
    public String vratHostGroupy() {
        return "{ \"result\": " + App.zabbixApi.getHostGroups() + " }";
    }

    // metoda vrati vsetkych hostov na zaklade zadaneho Host Gorup ID
    // metoda vrati json format
    public String vratHostov(String hostGroupId) {
        return "{ \"result\": " + App.zabbixApi.getHosts(hostGroupId) + " }";
    }

    // metoda vrati vsetky grafy na zaklade zadaneho hosta a popisu
    // metoda vrati json format
    public String vratGrafy(String hostName, String description) {
        return "{ \"result\": " + App.zabbixApi.getGraphs(hostName, description) + " }";
    }

    // metoda spracuje json format a vrati mapu s grafmi a ich ID
    public Map<String, String> parsujGrafy(String grafyJson) throws ParseException {
        Map<String, String> map = new HashMap<>();
        JSONParser parse = new JSONParser();
        JSONObject jsonObject = (JSONObject) parse.parse(grafyJson);
        JSONArray jsonArray = (JSONArray) jsonObject.get("result");
        for (Object o : jsonArray) {
            JSONObject graf = (JSONObject) o;
            map.put((String) graf.get("name"), (String) graf.get("graphid"));
        }
        return map;
    }

    // metoda sparsuje json format a vrati mapu s hostmi a ich ID
    public Map<String, String> parsujHostov(String hostsJson) throws ParseException {
        Map<String, String> map = new HashMap<>();
        JSONParser parse = new JSONParser();
        JSONObject jsonObject = (JSONObject) parse.parse(hostsJson);
        JSONArray jsonArray = (JSONArray) jsonObject.get("result");
        for (Object o : jsonArray) {
            JSONObject host = (JSONObject) o;
            map.put((String) host.get("host"), (String) host.get("hostid"));
        }
        return map;
    }

    // metoda sparsuje json format a vrati mapu s triggermi a ich ID
    public Map<String, String> parsujTriggery(String triggersJson) throws ParseException {
        Map<String, String> map = new HashMap<>();
        JSONParser parse = new JSONParser();
        JSONObject jsonObject = (JSONObject) parse.parse(triggersJson);
        JSONArray jsonArray = (JSONArray) jsonObject.get("result");
        for (Object o : jsonArray) {
            JSONObject trigger = (JSONObject) o;
            map.put((String) trigger.get("description"), (String) trigger.get("triggerid"));
        }
        return map;
    }

    // metoda sparsuje json format a vrati mapu s host groupami a ich ID
    public Map<String, String> parsujHostGroupy(String hostGroupJson) throws ParseException {
        Map<String, String> map = new HashMap<>();
        JSONParser parse = new JSONParser();
        JSONObject jsonObject = (JSONObject) parse.parse(hostGroupJson);
        JSONArray jsonArray = (JSONArray) jsonObject.get("result");
        for (Object o : jsonArray) {
            JSONObject hostGroup = (JSONObject) o;
            map.put((String) hostGroup.get("name"), (String) hostGroup.get("groupid"));
        }
        return map;
    }

}
