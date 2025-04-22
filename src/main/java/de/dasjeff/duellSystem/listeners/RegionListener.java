package de.dasjeff.duellSystem.listeners;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.dasjeff.duellSystem.DuellSystem;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Handles WorldGuard region protection override for duels
 */
public class RegionListener implements Listener {

    private final DuellSystem plugin;

    /**
     * Constructor
     * @param plugin Plugin instance
     */
    public RegionListener(DuellSystem plugin) {
        this.plugin = plugin;
    }

    // Handle entity damage by entity
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        
        // Check if both players are in the same duel and the duel has started
        if (plugin.getDuellManager().areInSameDuel(damaged.getUniqueId(), damager.getUniqueId()) &&
                plugin.getDuellManager().isDuelInProgress(damaged.getUniqueId())) {
            
            // Check if the event was cancelled due to region protection
            if (event.isCancelled() && isPvPDisabledInRegion(damaged.getLocation(), damager)) {
                // Override the cancellation
                event.setCancelled(false);
                plugin.debug("Overriding WorldGuard PvP protection for duel between " + 
                        damager.getName() + " and " + damaged.getName());
            }
        }
    }
    
    /**
     * Check if PvP is disabled in a region for a specific damager
     * @param location Location to check
     * @param damager The player attempting the action (damager)
     * @return True if PvP is disabled
     */
    private boolean isPvPDisabledInRegion(Location location, Player damager) {
        try {
            WorldGuardPlugin worldGuardPlugin = WorldGuardPlugin.inst();
            RegionQuery query = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            return !query.testState(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location), 
                    worldGuardPlugin.wrapPlayer(damager), Flags.PVP);
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking WorldGuard PvP flag: " + e.getMessage());
            return false;
        }
    }
}
