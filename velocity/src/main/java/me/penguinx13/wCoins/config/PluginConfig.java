package me.penguinx13.wCoins.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class PluginConfig {

    private static final String FILE_NAME = "config.properties";

    private final Path configPath;
    private final Properties properties;

    private PluginConfig(Path configPath, Properties properties) {
        this.configPath = configPath;
        this.properties = properties;
    }

    public static PluginConfig load(Path dataDirectory) throws IOException {
        Files.createDirectories(dataDirectory);
        Path path = dataDirectory.resolve(FILE_NAME);

        Properties defaults = new Properties();
        defaults.setProperty("database.host", "127.0.0.1");
        defaults.setProperty("database.port", "3306");
        defaults.setProperty("database.name", "wcoins");
        defaults.setProperty("database.user", "root");
        defaults.setProperty("database.password", "password");
        defaults.setProperty("database.pool.maximum", "10");

        Properties loaded = new Properties(defaults);
        if (Files.exists(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                loaded.load(input);
            }
        } else {
            try (OutputStream output = Files.newOutputStream(path)) {
                loaded.store(output, "WCoins configuration");
            }
        }

        return new PluginConfig(path, loaded);
    }

    public String getHost() {
        return properties.getProperty("database.host");
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("database.port"));
    }

    public String getDatabaseName() {
        return properties.getProperty("database.name");
    }

    public String getUser() {
        return properties.getProperty("database.user");
    }

    public String getPassword() {
        return properties.getProperty("database.password");
    }

    public int getMaximumPoolSize() {
        return Integer.parseInt(properties.getProperty("database.pool.maximum"));
    }

    public Path getConfigPath() {
        return configPath;
    }
}
