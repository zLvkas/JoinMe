package de.einluukas.joinme.commands;

import de.einluukas.joinme.config.ConfigManager;
import de.einluukas.joinme.database.DatabaseHelper;
import de.einluukas.joinme.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TokensCommand extends Command {

    private final DatabaseHelper databaseHelper;
    private final ConfigManager configManager;

    public TokensCommand(@NotNull DatabaseHelper databaseHelper, @NotNull ConfigManager configManager) {
        super("tokens");
        this.databaseHelper = databaseHelper;
        this.configManager = configManager;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length == 0 && commandSender instanceof ProxiedPlayer) {
            commandSender.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.TokenCommand.playerMessage")
                    .replace("%TOKENS%", String.valueOf(this.databaseHelper.getTokenAmountFromUser(((ProxiedPlayer) commandSender).getUniqueId())))));
            return;
        }

        if (!commandSender.hasPermission(this.configManager.getMessageFromConfig("Commands.TokenCommand.permission"))) {
            commandSender.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.TokenCommand.noPerm")));
            return;
        }

        if (strings.length >= 2) {
            UUID targetUniqueId;

            final ProxiedPlayer target = ProxyServer.getInstance().getPlayer(strings[1]);
            if (target != null) {
                targetUniqueId = target.getUniqueId();
            } else {
                targetUniqueId = UUIDFetcher.getUUIDFromName(strings[1]);
            }

            if (targetUniqueId == null) {
                commandSender.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.TokenCommand.targetNotFound").replace("%TARGET%", strings[1])));
                return;
            }

            if (strings.length == 2 && strings[0].equalsIgnoreCase("get")) {
                int amount = this.databaseHelper.getTokenAmountFromUser(targetUniqueId);
                commandSender.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.TokenCommand.targetTokens")
                        .replace("%TARGET%", strings[1])
                        .replace("%AMOUNT%", String.valueOf(amount == -1 ? 0 : amount))));
                return;
            }

            if (strings.length == 3) {
                int amount;
                try {
                    amount = Integer.parseInt(strings[2]);
                    if (amount < 0) {
                        commandSender.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.TokenCommand.notHigherThan0")));
                        return;
                    }

                } catch (NumberFormatException ignored) {
                    commandSender.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.TokenCommand.notANumber").replace("%NUMBER%", strings[2])));
                    return;
                }

                if (strings[0].equalsIgnoreCase("set")) {
                    int currentAmount = this.databaseHelper.getTokenAmountFromUser(targetUniqueId);
                    this.databaseHelper.updateUser(targetUniqueId, commandSender, amount, currentAmount == -1);
                    commandSender.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.TokenCommand.tokensSet")
                            .replace("%TARGET%", strings[1])
                            .replace("%AMOUNT%", String.valueOf(amount))));
                    return;
                }

                if (strings[0].equalsIgnoreCase("add")) {
                    int currentTokens = this.databaseHelper.getTokenAmountFromUser(targetUniqueId);
                    this.databaseHelper.updateUser(targetUniqueId, commandSender, currentTokens + amount, currentTokens == -1);
                    commandSender.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.TokenCommand.tokensAdded")
                            .replace("%TARGET%", strings[1])
                            .replace("%TOKENS%", String.valueOf(amount))));
                    return;
                }

                if (strings[0].equalsIgnoreCase("remove")) {
                    int currentTokens = this.databaseHelper.getTokenAmountFromUser(targetUniqueId);
                    if (currentTokens < amount) {
                        commandSender.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.TokenCommand.notEnoughTokens").replace("%TARGET%", strings[1])));
                        return;
                    }

                    this.databaseHelper.updateUser(targetUniqueId, commandSender, currentTokens - amount, false);
                    commandSender.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.TokenCommand.tokensRemoved")
                            .replace("%TARGET%", strings[1])
                            .replace("%TOKENS%", String.valueOf(amount))));
                    return;
                }
            }
        }

        commandSender.sendMessage(TextComponent.fromLegacyText(this.configManager.getMessageFromConfig("Commands.TokenCommand.usage")));
    }
}