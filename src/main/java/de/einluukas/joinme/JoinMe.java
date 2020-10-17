package de.einluukas.joinme;

import de.einluukas.joinme.cache.CacheManager;
import de.einluukas.joinme.commands.JoinMeCommand;
import de.einluukas.joinme.commands.TokensCommand;
import de.einluukas.joinme.config.ConfigManager;
import de.einluukas.joinme.database.DatabaseHelper;
import de.einluukas.joinme.database.DatabaseManager;
import de.einluukas.joinme.listeners.JoinMeListener;
import de.einluukas.joinme.utils.JoinMeManager;
import net.md_5.bungee.api.plugin.Plugin;

public class JoinMe extends Plugin {

    private final ConfigManager configManager = new ConfigManager();
    private final DatabaseManager databaseManager = new DatabaseManager();
    private final CacheManager cacheManager = new CacheManager();

    @Override
    public void onEnable() {
        this.configManager.init(this);

        this.databaseManager.initAndLoadConfig(this.configManager);
        this.databaseManager.connect();

        final DatabaseHelper databaseHelper = new DatabaseHelper(this.databaseManager, this.cacheManager);

        final JoinMeManager joinMeManager = new JoinMeManager(this.configManager);

        super.getProxy().getPluginManager().registerCommand(this, new TokensCommand(databaseHelper, this.configManager));
        super.getProxy().getPluginManager().registerCommand(this, new JoinMeCommand(this, databaseHelper, this.configManager, joinMeManager));

        super.getProxy().getPluginManager().registerListener(this, new JoinMeListener(joinMeManager));
    }

    @Override
    public void onDisable() {
        this.databaseManager.disconnect();
    }

}