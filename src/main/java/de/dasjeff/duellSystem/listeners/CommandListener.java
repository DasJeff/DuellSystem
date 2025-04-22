package de.dasjeff.duellSystem.listeners;

import de.dasjeff.duellSystem.DuellSystem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Blocks commands during duels
 */
public class CommandListener implements Listener {

    private final DuellSystem plugin;
    private final Set<String> allowedBaseCommands;

    /**
     * Constructor
     * @param plugin Plugin instance
     */
    public CommandListener(DuellSystem plugin) {
        this.plugin = plugin;
        // Store allowed commands
        this.allowedBaseCommands = Arrays.asList(
                "duel",
                "duell",
                "msg",
                "tell"
        ).stream().map(String::toLowerCase).collect(Collectors.toSet());
    }

    // Handle command preprocess
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // Check if player is in a duel
        if (plugin.getDuellManager().isInDuel(player.getUniqueId())) {
            String message = event.getMessage();
            String baseCommand = "";

            // Extract the base command
            if (message.length() > 1 && message.startsWith("/")) {
                int firstSpace = message.indexOf(' ');
                if (firstSpace == -1) {
                    baseCommand = message.substring(1).toLowerCase();
                } else {
                    baseCommand = message.substring(1, firstSpace).toLowerCase();
                }
            } else {
                // Treat as disallowed unless admin.
                baseCommand = "";
            }

            // Check if command is allowed
            if (!allowedBaseCommands.contains(baseCommand) && !player.hasPermission("duel.admin")) {
                event.setCancelled(true);
                plugin.getMessageManager().sendMessage(player, "general.command-blocked");
            }
        }
    }
}
