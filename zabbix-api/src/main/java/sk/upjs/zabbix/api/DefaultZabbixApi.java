package sk.upjs.zabbix.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultZabbixApi implements ZabbixApi {
    private static final Logger logger = LoggerFactory.getLogger(DefaultZabbixApi.class);

    private CloseableHttpClient httpClient;

    private URI uri;

    private volatile String auth;

    public DefaultZabbixApi(String url) {
        try {
            uri = new URI(url.trim());
        } catch (URISyntaxException e) {
            throw new RuntimeException("url invalid", e);
        }
    }

    public DefaultZabbixApi(URI uri) {
        this.uri = uri;
    }

    public DefaultZabbixApi(String url, CloseableHttpClient httpClient) {
        this(url);
        this.httpClient = httpClient;
    }

    public DefaultZabbixApi(URI uri, CloseableHttpClient httpClient) {
        this(uri);
        this.httpClient = httpClient;
    }

    @Override
    public void init() {
        if (httpClient == null) {
            try {
                // TODO: prerobit
                SSLContext sslContext = new SSLContextBuilder()
                        .loadTrustMaterial(null, (x509CertChain, authType) -> true)
                        .build();
                httpClient = HttpClients.custom().setSSLContext(sslContext).build();
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public void destroy() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                logger.error("close httpclient error!", e);
            }
        }
    }

    @Override
    public boolean login(String user, String password) {
        this.auth = null;
        Request request = RequestBuilder.newBuilder().paramEntry("user", user).paramEntry("password", password)
                .method("user.login").build();
        JSONObject response = call(request);
        String auth = response.getString("result");
        if (auth != null && !auth.isEmpty()) {
            this.auth = auth;
            return true;
        }
        return false;
    }

    @Override
    public String apiVersion() {
        Request request = RequestBuilder.newBuilder().method("apiinfo.version").build();
        JSONObject response = call(request);
        return response.getString("result");
    }

    @Override
    public String hostGet(String name) {
        HashMap<String, String> map = new HashMap<>();
        map.put("host", name);
        Request request = RequestBuilder.newBuilder().method("host.get").paramEntry("filter", map).build();
        JSONObject response = call(request);
        return response.getString("result");
    }

    @Override
    public String getItems(String name) {
        Request request = RequestBuilder.newBuilder().method("item.get").paramEntry("host", name).build();
        JSONObject response = call(request);
        return response.getString("result");
    }

    @Override
    public String getTriggers(String hostName, String description) {
        HashMap<String, String> map = new HashMap<>();
        map.put("description", description);
        Request request = RequestBuilder.newBuilder().method("trigger.get").paramEntry("host", hostName).paramEntry("search", map).build();
        JSONObject response = call(request);
        return response.getString("result");
    }

    @Override
    public String getHostGroups() {
        Request request = RequestBuilder.newBuilder().method("hostgroup.get").build();
        JSONObject response = call(request);
        return response.getString("result");
    }

    @Override
    public String getHosts(String groupId) {
        Request request = RequestBuilder.newBuilder().method("host.get").paramEntry("groupids", groupId).build();
        JSONObject response = call(request);
        return response.getString("result");
    }

    @Override
    public String getGraphs(String hostName, String description) {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", description);
        Request request = RequestBuilder.newBuilder().method("graph.get").paramEntry("host", hostName).paramEntry("search", map).build();
        JSONObject response = call(request);
        return response.getString("result");
    }

    @Override
    @SuppressWarnings("unchecked")
    public String createMap(String mapName, int height, int width, List<String> triggerID, List<String> graphID, Map<String, String> mapWithURL, String selectedGroupID, String selectedHostID) {
        JSONArray arrayFinal = new JSONArray();
        int counter = 1;
        int x = 110;
        for (int i = 0; i < triggerID.size(); i++) {
            JSONArray arrayWithId = new JSONArray();
            org.json.simple.JSONObject triggerObject = new org.json.simple.JSONObject();
            triggerObject.put("triggerid", triggerID.get(i));
            arrayWithId.add(triggerObject);


            JSONArray arrayWithGraph = new JSONArray();
            int j = i;
            mapWithURL.forEach((key, value) -> {
                String url = value;
                url = url.replace("{groupid}", selectedGroupID).replace("{hostid}", selectedHostID).replace("{graphid}", graphID.get(j));
                org.json.simple.JSONObject graphObject = new org.json.simple.JSONObject();
                graphObject.put("name", key);
                graphObject.put("url", url);
                arrayWithGraph.add(graphObject);

            });


//            org.json.simple.JSONObject graphObject = new org.json.simple.JSONObject();
//            graphObject.put("name", "Graph");
//
//            String url = "https://monitor.intrak.upjs.sk/charts.php?page=1&groupid=20&hostid=10280&graphid=" + graphID.get(i) + "&action=showgraph";
//            graphObject.put("url", url);
//
//            arrayWithGraph.add(graphObject);


            org.json.simple.JSONObject objectFinal = new org.json.simple.JSONObject();
            objectFinal.put("elements", arrayWithId);
            objectFinal.put("urls", arrayWithGraph);
            objectFinal.put("elementtype", "2");
            objectFinal.put("label", Integer.toString(i + 1));
            if (counter % 2 == 1) {
                objectFinal.put("label_location", "3");
                objectFinal.put("iconid_off", "195");
                objectFinal.put("iconid_disabled", "188");
                objectFinal.put("iconid_maintenance", "194");
                objectFinal.put("iconid_on", "196");
                objectFinal.put("y", "75");
                objectFinal.put("x", Integer.toString(x));
                counter++;
            } else {
                objectFinal.put("label_location", "0");
                objectFinal.put("iconid_off", "200");
                objectFinal.put("iconid_disabled", "197");
                objectFinal.put("iconid_maintenance", "199");
                objectFinal.put("iconid_on", "201");
                objectFinal.put("y", "150");
                objectFinal.put("x", Integer.toString(x));
                x += 50;
                counter++;
            }

            arrayFinal.add(objectFinal);
        }

        Request request = RequestBuilder.newBuilder().method("map.create")
                .paramEntry("name", mapName).paramEntry("height", height)
                .paramEntry("width", width).paramEntry("backgroundid", "192")
                .paramEntry("label_type", "0").paramEntry("expandproblem", 0)
                .paramEntry("selements", arrayFinal).build();
        JSONObject response = call(request);
        return response.getString("result");
    }

    @Override
    public JSONObject call(Request request) {
        if (request.getAuth() == null) {
            request.setAuth(this.auth);
        }

        try {
            String jsonString = JSON.toJSONString(request);
            StringEntity stringEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            HttpUriRequest httpRequest = org.apache.http.client.methods.RequestBuilder.post().setUri(uri)
                    .addHeader("Content-Type", "application/json")
                    .setEntity(stringEntity).build();
            CloseableHttpResponse response = httpClient.execute(httpRequest);
            HttpEntity entity = response.getEntity();
            byte[] data = EntityUtils.toByteArray(entity);
            return (JSONObject) JSON.parse(data);
        } catch (IOException e) {
            throw new RuntimeException("DefaultZabbixApi call exception!", e);
        }
    }

}
