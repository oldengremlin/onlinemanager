/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.ukrcom.onlinemanager;

/**
 *
 * @author olden
 */
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.swing.table.DefaultTableModel;

public class RestApiClient {

    private static volatile RestApiClient instance;

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private String token;

    // ====================== SINGLETON ======================
    private RestApiClient() throws IOException, ClassNotFoundException, InterruptedException {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
        // URL REST API — беремо з того ж jConfig (або окремого поля)
        jConfig config = new jConfigSerializing().load();
        this.baseUrl = config.getRestApiUrl(); // нове поле в jConfig
        login(config.getRestUsername(), config.getRestPassword());
    }

    public static RestApiClient getInstance() throws IOException, ClassNotFoundException, InterruptedException {
        RestApiClient local = instance;
        if (local == null) {
            synchronized (RestApiClient.class) {
                local = instance;
                if (local == null) {
                    instance = local = new RestApiClient();
                }
            }
        }
        return local;
    }

    // ====================== LOGIN ======================
    public boolean login(String username, String password) throws IOException, InterruptedException {
        String body = "username=" + encode(username) + "&password=" + encode(password);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            // відповідь: {"token":"uuid"}
            this.token = mapper.readTree(resp.body()).get("token").asText();
            return true;
        }
        return false;
    }

    // ====================== LOGOUT ======================
    public void logout() throws IOException, InterruptedException {
        JsonNode data = post("/logout", "");
    }

    // ====================== ОСНОВНИЙ ЗАПИТ ======================
    public RestApiClient getData(DefaultTableModel DFT, int days, boolean onlineOnly,
            int selectedIndex, String customerFilter, String usernameFilter)
            throws IOException, InterruptedException {

        String body = "days=" + days
                + "&onlineonly=" + onlineOnly
                + "&selectedindex=" + selectedIndex
                + "&customerfilter=" + encode(customerFilter)
                + "&usernamefilter=" + encode(usernameFilter);

        JsonNode data = post("/getdata", body);
        DFT.setRowCount(0);
        if (data != null && data.isArray()) {
            for (JsonNode row : data) {
                DFT.addRow(new Object[]{
                    row.get("radacctid").asText(),
                    row.get("username").asText(),
                    row.get("router").asText(),
                    row.get("sessionid").asText(),
                    row.get("acctstarttime").asText(),
                    row.get("acctupdatetime").asText(),
                    text(row, "acctstoptime"),
                    row.get("framedipaddress").asText(),
                    text(row, "dt"),
                    text(row, "customername"),
                    text(row, "location")
                });
            }
        }
        return this;
    }

    // ====================== ДУБЛІКАТИ ======================
    public RestApiClient getDuplicateData(DefaultTableModel DFT)
            throws IOException, InterruptedException {
        JsonNode data = post("/getduplicate", "");
        DFT.setRowCount(0);
        if (data != null && data.isArray()) {
            for (JsonNode row : data) {
                DFT.addRow(new Object[]{
                    row.get("username").asText(),
                    row.get("count").asText()
                });
            }
        }
        return this;
    }

    // ====================== ДЕТАЛІ ДУБЛІКАТІВ ======================
    public RestApiClient getDuplicateSessions(DefaultTableModel DFT, String username)
            throws IOException, InterruptedException {
        JsonNode data = post("/getduplicatesessions", "username=" + encode(username));
        DFT.setRowCount(0);
        if (data != null && data.isArray()) {
            for (JsonNode row : data) {
                DFT.addRow(new Object[]{
                    row.get("radacctid").asText(),
                    row.get("framedipaddress").asText(),
                    row.get("acctstarttime").asText(),
                    row.get("acctupdatetime").asText()
                });
            }
        }
        return this;
    }

    // ====================== КАНДИДАТ НА ЗАКРИТТЯ ======================
    public RestApiClient getAcctStopTimeCandidate(DefaultTableModel DFT,
            String startTime, String username, String ip)
            throws IOException, InterruptedException {
        String body = "starttime=" + encode(startTime)
                + "&username=" + encode(username)
                + "&ip=" + encode(ip);
        JsonNode data = post("/getacctstoptimecandidate", body);
        DFT.setRowCount(0);
        if (data != null && data.isArray()) {
            for (JsonNode row : data) {
                DFT.addRow(new Object[]{
                    row.get("acctstarttime").asText(),
                    row.get("acctstoptime").asText()
                });
            }
        }
        return this;
    }

    // ====================== КОРЕКЦІЯ ЧАСУ ======================
    public RestApiClient correctionAcctStopTime(Long id, String stopTime)
            throws IOException, InterruptedException {
        post("/correctionacctstoptime",
                "id=" + id + "&stoptime=" + encode(stopTime));
        return this;
    }

    // ====================== СПІЛЬНА ЛОГІКА POST ======================
    private JsonNode post(String path, String body) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(30))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(resp.body());

        if ("error".equals(root.path("status").asText())) {
            try {
                // спробуємо перелогінитись один раз
                jConfig config = new jConfigSerializing().load();
                if (login(config.getRestUsername(), config.getRestPassword())) {
                    resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                    root = mapper.readTree(resp.body());
                }
            } catch (ClassNotFoundException ex) {
                System.getLogger(RestApiClient.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }

        if (!"success".equals(root.path("status").asText())) {
            throw new IOException("REST error: " + root.path("error").asText());
        }
        return root.get("data");
    }

    private static String encode(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    // null-safe читання рядка з JSON
    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }
}
