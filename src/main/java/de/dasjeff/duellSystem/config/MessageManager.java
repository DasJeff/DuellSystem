package de.dasjeff.duellSystem.config;

import de.dasjeff.duellSystem.DuellSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the plugin messages using Adventure API
 */
public class MessageManager {

    private final DuellSystem plugin;
    private FileConfiguration messages;
    private File messagesFile;
    private Component prefix;

    /**
     * Constructor
     * @param plugin Plugin instance
     */
    public MessageManager(DuellSystem plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    // Load the messages
    private void loadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(messagesFile);
        prefix = colorize(messages.getString("prefix", "&8[&cDuell&8] &7"));
        plugin.getLogger().info("Nachrichten geladen!");
    }

    // Reload the messages
    public void reload() {
        loadMessages();
    }

    /**
     * Get a message component from the configuration
     * @param path Path to the message
     * @param placeholders Placeholders to replace
     * @return Formatted message component
     */
    public Component getMessage(String path, Map<String, String> placeholders) {
        String message = messages.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Message not found: " + path);
            return Component.text("Message not found: " + path);
        }

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        
        return prefix.append(colorize(message));
    }

    /**
     * Get a message component from the configuration
     * @param path Path to the message
     * @return Formatted message component
     */
    public Component getMessage(String path) {
        return getMessage(path, null);
    }

    /**
     * Get a title component from the configuration
     * @param path Path to the title
     * @param placeholders Placeholders to replace
     * @return Formatted title component
     */
    public Component getTitle(String path, Map<String, String> placeholders) {
        String title = messages.getString("titles." + path + ".title");
        if (title == null) {
             plugin.getLogger().warning("Title not found: " + path);
             return Component.empty();
        }

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                title = title.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        return colorize(title);
    }

    /**
     * Get a subtitle component from the configuration
     * @param path Path to the subtitle
     * @param placeholders Placeholders to replace
     * @return Formatted subtitle component
     */
    public Component getSubtitle(String path, Map<String, String> placeholders) {
        String subtitle = messages.getString("titles." + path + ".subtitle");
        if (subtitle == null) {
            plugin.getLogger().warning("Subtitle not found: " + path);
            return Component.empty();
        }

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                subtitle = subtitle.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        return colorize(subtitle);
    }

    /**
     * Send a message component to a player
     * @param player Player to send the message to
     * @param path Path to the message
     * @param placeholders Placeholders to replace
     */
    public void sendMessage(Player player, String path, Map<String, String> placeholders) {
        player.sendMessage(getMessage(path, placeholders));
    }

    /**
     * Send a message component to a player
     * @param player Player to send the message to
     * @param path Path to the message
     */
    public void sendMessage(Player player, String path) {
        sendMessage(player, path, null);
    }

    /**
     * Send a title to a player using Adventure API
     * @param player Player to send the title to
     * @param path Path to the title
     * @param placeholders Placeholders to replace
     * @param fadeIn Fade in time in ticks
     * @param stay Stay time in ticks
     * @param fadeOut Fade out time in ticks
     */
    public void sendTitle(Player player, String path, Map<String, String> placeholders, int fadeIn, int stay, int fadeOut) {
        Component titleComponent = getTitle(path, placeholders);
        Component subtitleComponent = getSubtitle(path, placeholders);

        Title.Times times = Title.Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut));

        Title title = Title.title(titleComponent, subtitleComponent, times);

        player.showTitle(title);
    }

    /**
     * Send a title to a player with default timings using Adventure API
     * @param player Player to send the title to
     * @param path Path to the title
     * @param placeholders Placeholders to replace
     */
    public void sendTitle(Player player, String path, Map<String, String> placeholders) {
        sendTitle(player, path, placeholders, 10, 70, 20); 
    }

    /**
     * Create a placeholder map
     * @return New placeholder map
     */
    public Map<String, String> createPlaceholderMap() {
        return new HashMap<>();
    }

    /**
     * Colorize a string using Adventure API
     * @param text Text to colorize (supports '&' codes)
     * @return Colorized component
     */
    private Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
