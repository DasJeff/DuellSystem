package de.dasjeff.duellSystem.config;

import de.dasjeff.duellSystem.DuellSystem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Manages the plugin configuration
 */
public class ConfigManager {

    private final DuellSystem plugin;
    private FileConfiguration config;
    private File configFile;

    /**
     * Constructor
     * @param plugin Plugin instance
     */
    public ConfigManager(DuellSystem plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    // Load the configuration
    private void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Konfiguration geladen!");
    }

    // Reload the configuration
    public void reload() {
        loadConfig();
    }

    /**
     * Get the maximum number of duels per player
     * @return Maximum number of duels per player
     */
    public int getMaxDuelsPerPlayer() {
        return config.getInt("settings.max-duels-per-player", 1);
    }

    /**
     * Get the countdown duration in seconds
     * @return Countdown duration in seconds
     */
    public int getCountdownDuration() {
        return config.getInt("settings.countdown-duration", 3);
    }

    /**
     * Get the proximity radius in blocks
     * @return Proximity radius in blocks
     */
    public int getProximityRadius() {
        return config.getInt("settings.proximity-radius", 10);
    }

    /**
     * Get the request timeout in seconds
     * @return Request timeout in seconds
     */
    public int getRequestTimeout() {
        return config.getInt("settings.request-timeout", 30);
    }

    /**
     * Get the minimum bet amount
     * @return Minimum bet amount
     */
    public double getMinBet() {
        return config.getDouble("economy.min-bet", 10);
    }

    /**
     * Get the maximum bet amount
     * @return Maximum bet amount
     */
    public double getMaxBet() {
        return config.getDouble("economy.max-bet", 10000);
    }

    /**
     * Check if players with negative balance can request duels
     * @return True if players with negative balance can request duels
     */
    public boolean allowNegativeBalance() {
        return config.getBoolean("economy.allow-negative-balance", false);
    }

    /**
     * Check if debug mode is enabled
     * @return True if debug mode is enabled
     */
    public boolean isDebugEnabled() {
        return config.getBoolean("debug", false);
    }
}
