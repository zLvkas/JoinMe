package de.einluukas.joinme.listeners;

import de.einluukas.joinme.utils.JoinMeManager;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class JoinMeListener implements Listener {

    private final JoinMeManager joinMeManager;

    public JoinMeListener(@NotNull JoinMeManager joinMeManager) {
        this.joinMeManager = joinMeManager;
    }

    @EventHandler
    public void handle(PlayerDisconnectEvent event) {
        this.joinMeManager.deleteJoinMe(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void handle(ServerSwitchEvent event) {
        this.joinMeManager.deleteJoinMe(event.getPlayer().getUniqueId());
    }

}