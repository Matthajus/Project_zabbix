module sk.upjs.zabbix {
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires java.desktop;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.slf4j;

    requires fastjson;
    requires json.simple;

    opens sk.upjs.zabbix to javafx.fxml;
    exports sk.upjs.zabbix;
}