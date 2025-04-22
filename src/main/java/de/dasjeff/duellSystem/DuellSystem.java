package de.dasjeff.duellSystem;

import de.dasjeff.duellSystem.commands.DuellCommand;
import de.dasjeff.duellSystem.config.ConfigManager;
import de.dasjeff.duellSystem.config.MessageManager;
import de.dasjeff.duellSystem.listeners.CommandListener;
import de.dasjeff.duellSystem.listeners.DuellListener;
import de.dasjeff.duellSystem.listeners.RegionListener;
import de.dasjeff.duellSystem.listeners.DuellWorldListener;
import de.dasjeff.duellSystem.managers.DuellManager;
import de.dasjeff.duellSystem.managers.EconomyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class DuellSystem extends JavaPlugin {

    private static DuellSystem instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private DuellManager duellManager;
    private EconomyManager economyManager;
    private Economy economy;
    private boolean worldGuardEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);

        // Setup economy
        if (!setupEconomy()) {
            getLogger().severe("Vault nicht gefunden oder keine Economy-Plugin installiert!");
            getLogger().severe("Deaktiviere Plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        this.economyManager = new EconomyManager(this);
        this.duellManager = new DuellManager(this);

        // Check for WorldGuard
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardEnabled = true;
            getLogger().info("WorldGuard gefunden - Region-Schutz-Override aktiviert");
        }

        // Register commands
        getCommand("duel").setExecutor(new DuellCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new DuellListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);
        getServer().getPluginManager().registerEvents(new DuellWorldListener(this), this);

        if (worldGuardEnabled) {
            getServer().getPluginManager().registerEvents(new RegionListener(this), this);
        }

        getLogger().info("Duell-System erfolgreich aktiviert!");
    }

    @Override
    public void onDisable() {
        // Cancel all active duels
        if (duellManager != null) {
            duellManager.cancelAllDuels();
        }

        getLogger().info("Duell-System deaktiviert!");
    }

    /**
     * Setup Vault Economy
     * @return true if economy was set up successfully
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    // Reload the plugin configuration
    public void reload() {
        configManager.reload();
        messageManager.reload();
        getLogger().info("Konfiguration neu geladen!");
    }

    /**
     * Get the plugin instance
     * @return DuellSystem instance
     */
    public static DuellSystem getInstance() {
        return instance;
    }

    /**
     * Get the config manager
     * @return ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get the message manager
     * @return MessageManager instance
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }

    /**
     * Get the duel manager
     * @return DuellManager instance
     */
    public DuellManager getDuellManager() {
        return duellManager;
    }

    /**
     * Get the economy manager
     * @return EconomyManager instance
     */
    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    /**
     * Get the economy instance
     * @return Economy instance
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Check if WorldGuard is enabled
     * @return true if WorldGuard is enabled
     */
    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }

    /**
     * Log a debug message if debug mode is enabled
     * @param message Message to log
     */
    public void debug(String message) {
        if (configManager.isDebugEnabled()) {
            getLogger().log(Level.INFO, "[DEBUG] " + message);
        }
    }
}
