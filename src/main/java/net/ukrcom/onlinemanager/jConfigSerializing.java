package net.ukrcom.onlinemanager;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.lang3.SystemUtils;

public class jConfigSerializing {

    public final String xmlName = "onlinemanager.config.xml";

    private File configFile;   // зберігаємо останній знайдений файл

    // ====================== SAVE ======================
    public void save(jConfig config) throws IOException {
        Path targetPath = getConfigFilePath().toAbsolutePath();

        // Створюємо папки, якщо їх немає
        Files.createDirectories(targetPath.getParent());

        new XmlMapper().writeValue(targetPath.toFile(), config);
        this.configFile = targetPath.toFile();   // запам'ятовуємо
    }

    // ====================== LOAD ======================
    public jConfig load() throws IOException, ClassNotFoundException {
        Path path = getConfigFilePath();
        this.configFile = path.toFile();

        return new XmlMapper().readValue(path.toFile(), jConfig.class);
    }

    // ====================== ОСНОВНА ЛОГІКА ======================
    private Path getConfigFilePath() throws IOException {
        // 1. Поточна директорія (для mvn exec:java)
        File f = new File(xmlName);
        if (f.exists()) {
            return f.toPath();
        }

        // 2. Поруч з JAR-ом (/opt/onlinemanager/bin/...)
        try {
            String path = getClass().getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            File jarDir = new File(path).getCanonicalFile().getParentFile();
            f = new File(jarDir, xmlName);
            if (f.exists()) {
                return f.toPath();
            }
        } catch (IOException ignored) {
        }

        // 3. Стандартні шляхи користувача
        File homeDir = new File(System.getProperty("user.home"));

        if (SystemUtils.IS_OS_LINUX) {
            f = new File(homeDir, ".config/onlinemanager/" + xmlName);
            if (f.exists()) {
                return f.toPath();
            }

            f = new File(homeDir, ".onlinemanager/" + xmlName);
            if (f.exists()) {
                return f.toPath();
            }
        } else if (SystemUtils.IS_OS_WINDOWS) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                f = new File(appData, "Onlinemanager\\" + xmlName);
                if (f.exists()) {
                    return f.toPath();
                }
            }
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null) {
                f = new File(localAppData, "Onlinemanager\\" + xmlName);
                if (f.exists()) {
                    return f.toPath();
                }
            }
        }

        // Якщо жоден не знайдено — повертаємо "найкращий" шлях для створення
        if (SystemUtils.IS_OS_LINUX) {
            return new File(homeDir, ".config/onlinemanager/" + xmlName).toPath();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            String appData = System.getenv("APPDATA");
            return new File(appData != null ? appData : homeDir.getPath(),
                    "Onlinemanager\\" + xmlName).toPath();
        }

        return new File(xmlName).toPath(); // fallback
    }

    // ====================== REINIT ======================
    public File getCurrentConfigFile() {
        return configFile;
    }
}
