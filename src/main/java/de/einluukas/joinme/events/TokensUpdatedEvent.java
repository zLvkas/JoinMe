package de.einluukas.joinme.events;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TokensUpdatedEvent extends Event {

    private final UUID playerUniqueId;

    private final CommandSender executor;

    private final int tokens;

    public TokensUpdatedEvent(@NotNull UUID playerUniqueId, @Nullable CommandSender executor, int tokens) {
        this.playerUniqueId = playerUniqueId;
        this.executor = executor;
        this.tokens = tokens;
    }

    @NotNull
    public UUID getPlayerUniqueId() {
        return this.playerUniqueId;
    }

    @Nullable
    public CommandSender getExecutor() {
        return this.executor;
    }

    public int getTokens() {
        return this.tokens;
    }
}