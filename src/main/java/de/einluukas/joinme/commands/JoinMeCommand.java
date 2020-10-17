package de.einluukas.joinme.commands;

import de.einluukas.joinme.JoinMe;
import de.einluukas.joinme.config.ConfigManager;
import de.einluukas.joinme.database.DatabaseHelper;
import de.einluukas.joinme.utils.JoinMeManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JoinMeCommand extends Command {

    private final JoinMe joinMe;
    private final DatabaseHelper databaseHelper;
    private final ConfigManager configManager;
    private final JoinMeManager joinMeManager;

    private final List<UUID> coolDown = new ArrayList<>();

    public JoinMeCommand(@NotNull JoinMe joinMe, @NotNull DatabaseHelper databaseHelper, @NotNull ConfigManager configManager, @NotNull JoinMeManager joinMeManager) {
        super("joinme");
        this.joinMe = joinMe;
        this.databaseHelper = databaseHelper;
        this.configManager = configManager;
        this.joinMeManager = joinMeManager;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (this.coolDown.contains(player.getUniqueId())) {
            player.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.JoinMeCommand.inCoolDown")));
            return;
        }

        if (strings.length == 0) {
            if (!player.hasPermission(this.configManager.getMessageFromConfig("Commands.JoinMeCommand.unlimitedTokenPermission"))) {
                int tokens = this.databaseHelper.getTokenAmountFromUser(player.getUniqueId());
                if (tokens <= 0) {
                    player.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.JoinMeCommand.notEnoughTokens")));
                    return;
                }

                this.databaseHelper.updateUser(player.getUniqueId(), null, tokens - 1, false);
            }

            if (!player.hasPermission(this.configManager.getMessageFromConfig("Commands.JoinMeCommand.noCoolDownPermission"))) {
                this.startCoolDown(player.getUniqueId());
            }

            this.joinMeManager.createJoinMe(player);
            return;
        }

        if (strings.length == 1) {
            final ProxiedPlayer target = ProxyServer.getInstance().getPlayer(strings[0]);
            if (target == null) {
                player.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.JoinMeCommand.noJoinMe")
                        .replace("%TARGET%", strings[0])));
                return;
            }

            final String joinMeServer = this.joinMeManager.getRunningJoinMe(target.getUniqueId());
            if (joinMeServer == null) {
                player.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.JoinMeCommand.noJoinMe")
                        .replace("%TARGET%", strings[0])));
                return;
            }

            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(joinMeServer);
            if (serverInfo == null) {
                player.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.JoinMeCommand.error")));
                return;
            }

            if (serverInfo.equals(player.getServer().getInfo())) {
                player.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.JoinMeCommand.alreadyConnected")
                        .replace("%PLAYER%", target.getDisplayName())));
                return;
            }

            player.connect(serverInfo);
            player.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.JoinMeCommand.connecting")
                    .replace("%PLAYER%", target.getDisplayName())));
            return;
        }

        player.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.JoinMeCommand.usage")));
    }

    private void startCoolDown(@NotNull UUID uniqueId) {
        this.coolDown.add(uniqueId);
        ProxyServer.getInstance().getScheduler().schedule(this.joinMe, () -> coolDown.remove(uniqueId), this.configManager.getIntFromConfig("Commands.JoinMeCommand.coolDownInSeconds"), TimeUnit.SECONDS);
    }

}