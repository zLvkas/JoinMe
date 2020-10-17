package de.einluukas.joinme.database;

import com.zaxxer.hikari.HikariDataSource;
import de.einluukas.joinme.config.ConfigManager;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

    private String host;

    private int port;

    private String database;

    private String username;

    private String password;

    private HikariDataSource hikariDataSource;

    public void initAndLoadConfig(@NotNull ConfigManager configManager) {
        this.host = configManager.getMessageFromConfig("database.host");
        this.port = configManager.getIntFromConfig("database.port");
        this.database = configManager.getMessageFromConfig("database.databaseName");
        this.username = configManager.getMessageFromConfig("database.databaseUser");
        this.password = configManager.getMessageFromConfig("database.databasePassword");
    }

    public void connect() {
        if (this.isConnected()) {
            return;
        }

        this.hikariDataSource = new HikariDataSource();
        this.hikariDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        this.hikariDataSource.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?serverTimezone=UTC", this.host, this.port, this.database));

        this.hikariDataSource.setUsername(this.username);
        this.hikariDataSource.setPassword(this.password);

        this.hikariDataSource.setValidationTimeout(5000);
        this.hikariDataSource.setConnectionTimeout(5000);
        this.hikariDataSource.setMaximumPoolSize(20);

        this.hikariDataSource.validate();
    }

    public void disconnect() {
        this.hikariDataSource.close();
        this.hikariDataSource = null;
    }

    public boolean isConnected() {
        return this.hikariDataSource != null && !this.hikariDataSource.isClosed();
    }

    @NotNull
    public Connection getConnection() throws SQLException {
        return this.hikariDataSource.getConnection();
    }

}