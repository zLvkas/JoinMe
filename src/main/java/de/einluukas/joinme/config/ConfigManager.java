package de.einluukas.joinme.config;

import de.einluukas.joinme.JoinMe;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManager {

    private final File configFile = new File("plugins/JoinMe/config.yml");
    private Configuration configuration;

    public void init(@NotNull JoinMe joinMe) {
        try {
            if (!Files.exists(Paths.get("plugins/JoinMe"))) {
                Files.createDirectory(Paths.get("plugins/JoinMe"));
            }

            if (!this.configFile.exists()) {
                try (InputStream inputStream = joinMe.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (inputStream == null) {
                        return;
                    }

                    Files.copy(inputStream, Paths.get("plugins/JoinMe/config.yml"));
                }
            }

            this.configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.configFile);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    @NotNull
    public String getMessageFromConfig(@NotNull String name) {
        return this.configuration.getString(name, "Message-Key " + name + " not found!")
                .replace("&", "§")
                .replace("%NEW%", "\n")
                .replace("%PREFIX%", this.configuration.getString("Commands.JoinMeCommand.prefix"))
                .replace("%TOKENPREFIX%", this.configuration.getString("Commands.TokenCommand.prefix"));
    }

    public int getIntFromConfig(@NotNull String name) {
        return this.configuration.getInt(name);
    }

}