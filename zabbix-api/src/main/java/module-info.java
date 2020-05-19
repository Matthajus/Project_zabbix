module sk.upjs.zabbix {
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires java.desktop;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.slf4j;
    requires java.sql;

    requires fastjson;
    requires json.simple;

    opens sk.upjs.zabbix to javafx.fxml;
    opens sk.upjs.zabbix.api to fastjson;

    exports sk.upjs.zabbix;
    exports sk.upjs.zabbix.api;
}