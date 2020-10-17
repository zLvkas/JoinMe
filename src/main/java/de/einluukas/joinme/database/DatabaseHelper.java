package de.einluukas.joinme.database;

import de.einluukas.joinme.cache.CacheManager;
import de.einluukas.joinme.events.TokensUpdatedEvent;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseHelper {

    private final DatabaseManager databaseManager;
    private final CacheManager cacheManager;

    public DatabaseHelper(@NotNull DatabaseManager databaseManager, @NotNull CacheManager cacheManager) {
        this.databaseManager = databaseManager;
        this.cacheManager = cacheManager;

        try (Connection connection = this.databaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `JoinMe` (`uniqueId` VARCHAR(36), `tokens` INT, PRIMARY KEY (`uniqueId`));")) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean containsUser(@NotNull UUID uniqueId) {
        try (Connection connection = this.databaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `JoinMe` WHERE `uniqueId` = ?")) {
            preparedStatement.setString(1, uniqueId.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public void insertUser(@NotNull UUID uniqueId, int tokens) {
        try (Connection connection = this.databaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `JoinMe`(`uniqueId`, `tokens`) VALUES (?, ?);")) {
            preparedStatement.setString(1, uniqueId.toString());
            preparedStatement.setInt(2, tokens);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int getTokenAmountFromUser(@NotNull UUID uniqueId) {
        Integer tokens = cacheManager.getTokens(uniqueId);
        if (tokens != null) {
            return tokens;
        }

        try (Connection connection = this.databaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `JoinMe` WHERE `uniqueId` = ?;")) {
            preparedStatement.setString(1, uniqueId.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            int amount = resultSet.next() ? resultSet.getInt("tokens") : -1;
            if (amount != -1) {
                this.cacheManager.insertIntoCache(uniqueId, amount);
            }

            return amount;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public void updateUser(@NotNull UUID uniqueId, @Nullable CommandSender commandSender, int tokens, boolean insert) {
        ProxyServer.getInstance().getPluginManager().callEvent(new TokensUpdatedEvent(uniqueId, commandSender, tokens));
        this.cacheManager.insertIntoCache(uniqueId, tokens);
        if (insert) {
            this.insertUser(uniqueId, tokens);
            return;
        }

        try (Connection connection = this.databaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `JoinMe` SET `tokens`= ? WHERE `uniqueId` = ?;")) {
            preparedStatement.setInt(1, tokens);
            preparedStatement.setString(2, uniqueId.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}