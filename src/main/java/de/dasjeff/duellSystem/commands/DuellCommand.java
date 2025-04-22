package de.dasjeff.duellSystem.commands;

import de.dasjeff.duellSystem.DuellSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the /duel command
 */
public class DuellCommand implements CommandExecutor, TabCompleter {

    private final DuellSystem plugin;

    /**
     * Constructor
     * @param plugin Plugin instance
     */
    public DuellCommand(DuellSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        // Handle subcommands
        switch (args[0].toLowerCase()) {
            case "accept":
                handleAccept(player, args);
                break;
            case "reload":
                handleReload(player);
                break;
            default:
                handleRequest(player, args);
                break;
        }

        return true;
    }

    /**
     * Handle the /duel accept command
     * @param player Player executing the command
     * @param args Command arguments
     */
    private void handleAccept(Player player, String[] args) {
        if (!player.hasPermission("duel.accept")) {
            plugin.getMessageManager().sendMessage(player, "general.no-permission");
            return;
        }

        if (args.length < 2) {
            plugin.getMessageManager().sendMessage(player, "general.accept_usage");
            return;
        }

        // Check if player is already in a duel
        if (plugin.getDuellManager().isInDuel(player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, "general.already-in-duel");
            return;
        }

        // Get the target player
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            plugin.getMessageManager().sendMessage(player, "general.player-not-found", 
                    createPlaceholderMap("player", args[1]));
            return;
        }

        // Check if target is already in a duel
        if (plugin.getDuellManager().isInDuel(target.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, "general.target-already-in-duel", 
                    createPlaceholderMap("player", target.getName()));
            return;
        }

        // Accept the request
        plugin.getDuellManager().acceptRequest(player, target);
    }

    /**
     * Handle the /duel reload command
     * @param player Player executing the command
     */
    private void handleReload(Player player) {
        if (!player.hasPermission("duel.admin")) {
            plugin.getMessageManager().sendMessage(player, "general.no-permission");
            return;
        }

        plugin.reload();
        plugin.getMessageManager().sendMessage(player, "general.reload");
    }

    /**
     * Handle the /duel <player> [amount] command
     * @param player Player executing the command
     * @param args Command arguments
     */
    private void handleRequest(Player player, String[] args) {
        if (!player.hasPermission("duel.request")) {
            plugin.getMessageManager().sendMessage(player, "general.no-permission");
            return;
        }

        // Check if player is already in a duel
        if (plugin.getDuellManager().isInDuel(player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, "general.already-in-duel");
            return;
        }

        // Get the target player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            plugin.getMessageManager().sendMessage(player, "general.player-not-found", 
                    createPlaceholderMap("player", args[0]));
            return;
        }

        // Check if target is the same as sender
        if (target.equals(player)) {
            plugin.getMessageManager().sendMessage(player, "general.cannot-duel-self");
            return;
        }

        // Check if target is already in a duel
        if (plugin.getDuellManager().isInDuel(target.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, "general.target-already-in-duel", 
                    createPlaceholderMap("player", target.getName()));
            return;
        }

        // Parse bet amount
        double betAmount = 0;
        if (args.length > 1) {
            try {
                betAmount = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                plugin.getMessageManager().sendMessage(player, "request.invalid-amount", 
                        createPlaceholderMap("min", String.valueOf(plugin.getConfigManager().getMinBet()), 
                                "max", String.valueOf(plugin.getConfigManager().getMaxBet())));
                return;
            }

            // Check if bet amount is valid
            if (betAmount < plugin.getConfigManager().getMinBet() || betAmount > plugin.getConfigManager().getMaxBet()) {
                plugin.getMessageManager().sendMessage(player, "request.invalid-amount", 
                        createPlaceholderMap("min", String.valueOf(plugin.getConfigManager().getMinBet()), 
                                "max", String.valueOf(plugin.getConfigManager().getMaxBet())));
                return;
            }
        }

        // Create the request
        plugin.getDuellManager().createRequest(player, target, betAmount);
    }

    /**
     * Send usage information to a player
     * @param player Player to send the usage to
     */
    private void sendUsage(Player player) {
        player.sendMessage(plugin.getMessageManager().getMessage("usage.header"));
        player.sendMessage(plugin.getMessageManager().getMessage("usage.line.request"));
        player.sendMessage(plugin.getMessageManager().getMessage("usage.line.request_bet"));
        player.sendMessage(plugin.getMessageManager().getMessage("usage.line.accept"));
        if (player.hasPermission("duel.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage("usage.line.reload"));
        }
        player.sendMessage(plugin.getMessageManager().getMessage("usage.footer"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            
            // Add subcommands
            completions.add("accept");
            if (player.hasPermission("duel.admin")) {
                completions.add("reload");
            }
            
            // Add online players
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(player))
                    .map(Player::getName)
                    .collect(Collectors.toList()));
            
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("accept")) {
                // Only show players who have sent a request
                return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(player))
                        .filter(p -> plugin.getDuellManager().hasPendingRequest(player.getUniqueId(), p.getUniqueId()))
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else {
                // Suggest bet amounts
                return Arrays.asList("10", "50", "100", "500", "1000").stream()
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
    
    /**
     * Create a placeholder map with a single entry
     * @param key Key
     * @param value Value
     * @return The placeholder map
     */
    private Map<String, String> createPlaceholderMap(String key, String value) {
        Map<String, String> placeholders = plugin.getMessageManager().createPlaceholderMap();
        placeholders.put(key, value);
        return placeholders;
    }
    
    /**
     * Create a placeholder map with two entries
     * @param key1 First key
     * @param value1 First value
     * @param key2 Second key
     * @param value2 Second value
     * @return The placeholder map
     */
    private Map<String, String> createPlaceholderMap(String key1, String value1, String key2, String value2) {
        Map<String, String> placeholders = plugin.getMessageManager().createPlaceholderMap();
        placeholders.put(key1, value1);
        placeholders.put(key2, value2);
        return placeholders;
    }
}
