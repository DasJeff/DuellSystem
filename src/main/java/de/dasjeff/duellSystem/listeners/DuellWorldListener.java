package de.dasjeff.duellSystem.listeners;

import de.dasjeff.duellSystem.DuellSystem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Prevents players from interacting with the world while in an active duel.
 */
public class DuellWorldListener implements Listener {

    private final DuellSystem plugin;

    public DuellWorldListener(DuellSystem plugin) {
        this.plugin = plugin;
    }

    /**
     * Prevents players in an active duel from breaking blocks.
     * @param event BlockBreakEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDuellManager().isDuelInProgress(player.getUniqueId())) {
            event.setCancelled(true);
            sendDisabledMessage(player);
        }
    }

    /**
     * Prevents players in an active duel from placing blocks.
     * @param event BlockPlaceEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDuellManager().isDuelInProgress(player.getUniqueId())) {
            event.setCancelled(true);
            sendDisabledMessage(player);
        }
    }
    
    /**
     * Prevents players in an active duel from interacting.
     * @param event PlayerInteractEvent
     */
     @EventHandler(ignoreCancelled = true)
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         if (plugin.getDuellManager().isDuelInProgress(player.getUniqueId())) {
             if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                 Material clickedType = event.getClickedBlock().getType();
                 if (!isAllowedInteraction(clickedType)) { 
                     event.setCancelled(true);
                     sendDisabledMessage(player);
                 }
             }
         }
     }

    /**
     * Helper method to check if interaction with a specific material should be allowed during a duel.
     * For now, no interaction is allowed.
     * @param material The material being interacted with.
     * @return True if interaction is allowed, false otherwise.
     */
    private boolean isAllowedInteraction(Material material) {
        return false; 
    }

    /**
     * Sends the "interaction disabled" message to the player.
     * @param player The player to send the message to.
     */
    private void sendDisabledMessage(Player player) {
        plugin.getMessageManager().sendMessage(player, "duel.interaction-disabled"); 
    }
} 