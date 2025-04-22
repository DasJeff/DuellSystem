package de.dasjeff.duellSystem.managers;

import de.dasjeff.duellSystem.DuellSystem;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Manages economy operations
 */
public class EconomyManager {

    private final DuellSystem plugin;

    /**
     * Constructor
     * @param plugin Plugin instance
     */
    public EconomyManager(DuellSystem plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if a player has enough money
     * @param player Player to check
     * @param amount Amount to check
     * @return True if the player has enough money
     */
    public boolean hasEnoughMoney(Player player, double amount) {
        if (amount <= 0) return true;
        
        if (!plugin.getConfigManager().allowNegativeBalance() && plugin.getEconomy().getBalance(player) < amount) {
            return false;
        }
        
        return true;
    }

    /**
     * Transfer money from one player to another
     * @param fromId UUID of the player to take money from
     * @param toId UUID of the player to give money to
     * @param amount Amount to transfer
     * @return True if the transfer was successful
     */
    public boolean transferMoney(UUID fromId, UUID toId, double amount) {
        if (amount <= 0) return true;
        
        OfflinePlayer from = Bukkit.getOfflinePlayer(fromId);
        OfflinePlayer to = Bukkit.getOfflinePlayer(toId);
        
        // Withdraw from loser
        if (!plugin.getEconomy().withdrawPlayer(from, amount).transactionSuccess()) {
            plugin.getLogger().warning("Failed to withdraw " + amount + " from player " + from.getName());
            return false;
        }
        
        // Deposit to winner
        if (!plugin.getEconomy().depositPlayer(to, amount).transactionSuccess()) {
            plugin.getLogger().warning("Failed to deposit " + amount + " to player " + to.getName() + ". Attempting refund...");
            
            // Refund the loser
            if (!plugin.getEconomy().depositPlayer(from, amount).transactionSuccess()) {
                // Log severe error if refund fails
                plugin.getLogger().severe("CRITICAL: Failed to refund " + amount + " to player " + from.getName() + 
                        " after failed deposit to " + to.getName() + ". Money may have been lost!");
            } else {
                plugin.getLogger().info("Successfully refunded " + amount + " to player " + from.getName());
            }
            return false;
        }
        
        return true;
    }
}
