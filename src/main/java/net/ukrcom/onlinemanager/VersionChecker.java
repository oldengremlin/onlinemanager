/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.ukrcom.onlinemanager;

import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author olden
 */
public class VersionChecker {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/oldengremlin/onlinemanager/releases/latest";
    private static final String RELEASES_URL = "https://github.com/oldengremlin/onlinemanager/releases";

    /**
     * Перевіряє оновлення і показує діалог, якщо є новіша версія
     *
     * @param parentFrame вікно-власник для JOptionPane
     */
    public static void checkForUpdates(javax.swing.JFrame parentFrame) {
        String currentVersion = getCurrentVersion();
        if (currentVersion == null || currentVersion.isEmpty()) {
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GITHUB_API_URL))
                .header("Accept", "application/vnd.github.v3+json")
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String json = response.body();
                String latestTag = extractTagFromJson(json);

                if (latestTag != null && isNewerVersion(latestTag, currentVersion)) {
                    int choice = JOptionPane.showConfirmDialog(
                            parentFrame,
                            "New version available: " + latestTag + "\n\nOpen releases page?",
                            "Update available",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        openInBrowser(RELEASES_URL);
                    }
                }
            }
        } catch (HeadlessException | IOException | InterruptedException e) {
            // тихо ігноруємо — не хочемо ламати запуск
            System.err.println("Failed to check for updates.: " + e.getMessage());
        }
    }

    /**
     * Читає версію з pom.properties (генерується jpackage)
     */
    private static String getCurrentVersion() {
        try (InputStream is = VersionChecker.class.getResourceAsStream("/app/maven-archiver/pom.properties")) {
            if (is == null) {
                return null;
            }

            Properties props = new Properties();
            props.load(is);
            return props.getProperty("version");
        } catch (IOException e) {
            System.err.println("Failed to read pom.properties: " + e.getMessage());
            return null;
        }
    }

    private static String extractTagFromJson(String json) {
        String key = "\"tag_name\":\"";
        int start = json.indexOf(key);
        if (start == -1) {
            return null;
        }
        start += key.length();
        int end = json.indexOf("\"", start);
        if (end == -1) {
            return null;
        }
        return json.substring(start, end);
    }

    private static boolean isNewerVersion(String latest, String current) {
        latest = latest.replace("v", "").trim();
        current = current.replace("v", "").trim();

        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");

        for (int i = 0; i < Math.min(latestParts.length, currentParts.length);
                i++) {
            try {
                int l = Integer.parseInt(latestParts[i]);
                int c = Integer.parseInt(currentParts[i]);
                if (l > c) {
                    return true;
                }
                if (l < c) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return latest.compareTo(current) > 0; // fallback на строкове порівняння
            }
        }
        return latestParts.length > currentParts.length;
    }

    private static void openInBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not open browser..\n\nOpen manually:\n" + url,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
