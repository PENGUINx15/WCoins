package me.penguinx13.wCoins.currency;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.penguinx13.wCoins.config.PluginConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private HikariDataSource dataSource;

    public void connect(PluginConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabaseName() + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        hikariConfig.setUsername(config.getUser());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
        hikariConfig.setPoolName("WCoinsPool");

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public void initializeSchema() {
        String sql = """
                CREATE TABLE IF NOT EXISTS balances (
                    player_uuid VARCHAR(36) PRIMARY KEY,
                    balance BIGINT NOT NULL DEFAULT 0
                )
                """;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize schema", exception);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
