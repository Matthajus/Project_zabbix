package io.github.hengyunabc.zabbix.api;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface ZabbixApi {

	void init();

	void destroy();

	String apiVersion();

	JSONObject call(Request request);

	boolean login(String user, String password);

	String hostGet(String name);

	String getItems(String name);

	String getTriggers(String name, String description);

	String getHostGroups();

	String getHosts(String groupId);

	String createMap(String mapName, int height, int width, List<String> triggerID, List<String> graphID, Map<String, String> mapWithURL, String selectedGroupID, String SelectedHostID);

	String getGraphs(String hostName, String description);
}
