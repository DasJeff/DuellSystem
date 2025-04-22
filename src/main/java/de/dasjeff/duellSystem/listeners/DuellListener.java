package de.dasjeff.duellSystem.listeners;

import de.dasjeff.duellSystem.DuellSystem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles events related to duels
 */
public class DuellListener implements Listener {

    private final DuellSystem plugin;

    /**
     * Constructor
     * @param plugin Plugin instance
     */
    public DuellListener(DuellSystem plugin) {
        this.plugin = plugin;
    }

    // Handle player death
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Check if player is in a duel
        if (plugin.getDuellManager().isInDuel(player.getUniqueId())) {
            // Get the killer
            Player killer = player.getKiller();
            if (killer == null || !plugin.getDuellManager().areInSameDuel(player.getUniqueId(), killer.getUniqueId())) {
                // End the duel with the player as loser
                plugin.getDuellManager().handlePlayerQuit(player.getUniqueId());
            } else {
                // End the duel with the killer as winner
                plugin.getDuellManager().endDuel(killer.getUniqueId(), player.getUniqueId());
            }
            
            // Keep inventory and exp
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    // Handle player quit
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getDuellManager().isInDuel(player.getUniqueId())) {
            // Handle player quit
            plugin.getDuellManager().handlePlayerQuit(player.getUniqueId());
        }
    }

    // Handle entity damage by entity
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        if (plugin.getDuellManager().areInSameDuel(damaged.getUniqueId(), damager.getUniqueId())) {
            // Check if the duel has started
            if (!plugin.getDuellManager().isDuelInProgress(damaged.getUniqueId())) {
                // Cancel damage during countdown
                event.setCancelled(true);
            }
            
        } else if (plugin.getDuellManager().isInDuel(damaged.getUniqueId()) || plugin.getDuellManager().isInDuel(damager.getUniqueId())) {
            // Cancel damage to prevent interference
            event.setCancelled(true);
        }
    }
}
