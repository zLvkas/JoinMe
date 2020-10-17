package de.einluukas.joinme.utils;

import de.einluukas.joinme.config.ConfigManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JoinMeManager {

    private final ConfigManager configManager;
    private final Map<UUID, String> runningJoinMes = new HashMap<>();

    public JoinMeManager(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void createJoinMe(@NotNull ProxiedPlayer creator) {
        this.runningJoinMes.put(creator.getUniqueId(), creator.getServer().getInfo().getName());

        final TextComponent message = new TextComponent();
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/JoinMe " + creator.getName()));
        message.setText(this.configManager.getMessageFromConfig("Commands.JoinMeCommand.broadCastMessage")
                .replace("%SERVER%", creator.getServer().getInfo().getName())
                .replace("%PLAYER%", creator.getDisplayName()));

        ProxyServer.getInstance().broadcast(message);
        creator.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.JoinMeCommand.joinMeCreated")
                .replace("%AMOUNT%", String.valueOf(ProxyServer.getInstance().getOnlineCount()))));
    }

    public void deleteJoinMe(@NotNull UUID uniqueId) {
        this.runningJoinMes.remove(uniqueId);
    }

    @Nullable
    public String getRunningJoinMe(@NotNull UUID targetUniqueId) {
        if (!this.runningJoinMes.containsKey(targetUniqueId)) {
            return null;
        }

        return this.runningJoinMes.get(targetUniqueId);
    }

}