package de.dasjeff.duellSystem.managers;

import de.dasjeff.duellSystem.DuellSystem;
import de.dasjeff.duellSystem.model.DuellRequest;
import de.dasjeff.duellSystem.model.DuellSession;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all duels and duel requests
 */
public class DuellManager {

    private final DuellSystem plugin;
    private final Map<UUID, Set<DuellRequest>> pendingRequests;
    private final Map<UUID, Set<DuellRequest>> sentRequests;
    private final Map<UUID, DuellSession> activeDuels;
    private final Map<UUID, BukkitTask> requestTimeoutTasks;

    /**
     * Constructor
     * @param plugin Plugin instance
     */
    public DuellManager(DuellSystem plugin) {
        this.plugin = plugin;
        this.pendingRequests = new ConcurrentHashMap<>();
        this.sentRequests = new ConcurrentHashMap<>();
        this.activeDuels = new ConcurrentHashMap<>();
        this.requestTimeoutTasks = new ConcurrentHashMap<>();
    }

    /**
     * Create a new duel request
     * @param sender Player sending the request
     * @param target Player receiving the request
     * @param betAmount Bet amount (0 for friendly duel)
     * @return True if the request was created successfully
     */
    public boolean createRequest(Player sender, Player target, double betAmount) {
        // Check if sender can afford the bet
        if (betAmount > 0 && !plugin.getEconomyManager().hasEnoughMoney(sender, betAmount)) {
            plugin.getMessageManager().sendMessage(sender, "request.not-enough-money", 
                    createPlaceholderMap("amount", String.valueOf(betAmount)));
            return false;
        }
        
        // Check if target can afford the bet
        if (betAmount > 0 && !plugin.getEconomyManager().hasEnoughMoney(target, betAmount)) {
            plugin.getMessageManager().sendMessage(sender, "request.target-not-enough-money", 
                    createPlaceholderMap("player", target.getName()));
            return false;
        }
        
        // Check if players are close enough
        if (!isPlayerInRange(sender, target)) {
            plugin.getMessageManager().sendMessage(sender, "request.too-far-away", 
                    createPlaceholderMap("player", target.getName(), 
                            "distance", String.valueOf(plugin.getConfigManager().getProximityRadius())));
            return false;
        }
        
        // Create the request
        DuellRequest request = new DuellRequest(sender.getUniqueId(), target.getUniqueId(), betAmount);
        
        // Add to pending requests
        pendingRequests.computeIfAbsent(target.getUniqueId(), k -> new HashSet<>()).add(request);
        sentRequests.computeIfAbsent(sender.getUniqueId(), k -> new HashSet<>()).add(request);
        
        // Schedule timeout task
        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            boolean removedFromPending = false;
            if (pendingRequests.containsKey(target.getUniqueId())) {
                if (pendingRequests.get(target.getUniqueId()).remove(request)) {
                    removedFromPending = true;
                    if (pendingRequests.get(target.getUniqueId()).isEmpty()) {
                        pendingRequests.remove(target.getUniqueId());
                    }
                }
            }
            
            boolean removedFromSent = false;
            if (sentRequests.containsKey(sender.getUniqueId())) {
                if (sentRequests.get(sender.getUniqueId()).remove(request)) {
                    removedFromSent = true;
                    if (sentRequests.get(sender.getUniqueId()).isEmpty()) {
                        sentRequests.remove(sender.getUniqueId());
                    }
                }
            }
            
            requestTimeoutTasks.remove(request.getId());
            
            if (removedFromPending || removedFromSent) {
                Player senderPlayer = Bukkit.getPlayer(request.getSenderId());
                if (senderPlayer != null && senderPlayer.isOnline()) {
                    plugin.getMessageManager().sendMessage(senderPlayer, "request.expired", 
                            createPlaceholderMap("player", target.getName()));
                }
            }
        }, plugin.getConfigManager().getRequestTimeout() * 20L);
        
        requestTimeoutTasks.put(request.getId(), timeoutTask);
        
        // Send messages
        Map<String, String> placeholders = createPlaceholderMap("player", target.getName());
        if (betAmount > 0) {
            placeholders.put("amount", String.valueOf(betAmount));
            plugin.getMessageManager().sendMessage(sender, "request.sent-with-bet", placeholders);
            
            placeholders = createPlaceholderMap("player", sender.getName(), "amount", String.valueOf(betAmount));
            plugin.getMessageManager().sendMessage(target, "request.received-with-bet", placeholders);
        } else {
            plugin.getMessageManager().sendMessage(sender, "request.sent", placeholders);
            
            placeholders = createPlaceholderMap("player", sender.getName());
            plugin.getMessageManager().sendMessage(target, "request.received", placeholders);
        }
        
        return true;
    }
    
    /**
     * Accept a duel request
     * @param target Player accepting the request
     * @param sender Player who sent the request
     * @return True if the request was accepted successfully
     */
    public boolean acceptRequest(Player target, Player sender) {
        // Check if there is a pending request
        if (!hasPendingRequest(target.getUniqueId(), sender.getUniqueId())) {
            plugin.getMessageManager().sendMessage(target, "request.no-pending-request", 
                    createPlaceholderMap("player", sender.getName()));
            return false;
        }
        
        // Get the request
        DuellRequest request = getPendingRequest(target.getUniqueId(), sender.getUniqueId());
        if (request == null) {
            plugin.getMessageManager().sendMessage(target, "request.no-pending-request", 
                    createPlaceholderMap("player", sender.getName()));
            return false;
        }
        
        // Check if players are close enough
        if (!isPlayerInRange(target, sender)) {
            plugin.getMessageManager().sendMessage(target, "request.too-far-away", 
                    createPlaceholderMap("player", sender.getName(), 
                            "distance", String.valueOf(plugin.getConfigManager().getProximityRadius())));
            return false;
        }
        
        // Check if players can still afford the bet
        double betAmount = request.getBetAmount();
        if (betAmount > 0) {
            if (!plugin.getEconomyManager().hasEnoughMoney(sender, betAmount)) {
                plugin.getMessageManager().sendMessage(target, "request.target-not-enough-money", 
                        createPlaceholderMap("player", sender.getName()));
                return false;
            }
            
            if (!plugin.getEconomyManager().hasEnoughMoney(target, betAmount)) {
                plugin.getMessageManager().sendMessage(target, "request.not-enough-money", 
                        createPlaceholderMap("amount", String.valueOf(betAmount)));
                return false;
            }
        }
        
        // Remove the request
        boolean removedFromPending = false;
        if (pendingRequests.containsKey(target.getUniqueId())) {
            if (pendingRequests.get(target.getUniqueId()).remove(request)) {
                removedFromPending = true;
                if (pendingRequests.get(target.getUniqueId()).isEmpty()) {
                    pendingRequests.remove(target.getUniqueId());
                }
            }
        }
        
        boolean removedFromSent = false;
        if (sentRequests.containsKey(sender.getUniqueId())) {
            if (sentRequests.get(sender.getUniqueId()).remove(request)) {
                removedFromSent = true;
                if (sentRequests.get(sender.getUniqueId()).isEmpty()) {
                    sentRequests.remove(sender.getUniqueId());
                }
            }
        }
        
        // Cancel the timeout task
        if (requestTimeoutTasks.containsKey(request.getId())) {
            requestTimeoutTasks.get(request.getId()).cancel();
            requestTimeoutTasks.remove(request.getId());
        }
        
        // Send messages
        Map<String, String> placeholders = createPlaceholderMap("player", sender.getName());
        plugin.getMessageManager().sendMessage(target, "duel.accepted", placeholders);
        
        placeholders = createPlaceholderMap("player", target.getName());
        plugin.getMessageManager().sendMessage(sender, "duel.target-accepted", placeholders);
        
        // Start the duel
        startDuel(sender, target, betAmount);
        
        return true;
    }
    
    /**
     * Start a duel between two players
     * @param player1 First player
     * @param player2 Second player
     * @param betAmount Bet amount
     */
    private void startDuel(Player player1, Player player2, double betAmount) {
        // Create the duel session
        DuellSession session = new DuellSession(player1.getUniqueId(), player2.getUniqueId(), betAmount);
        
        // Add to active duels
        activeDuels.put(player1.getUniqueId(), session);
        activeDuels.put(player2.getUniqueId(), session);
        
        // Start countdown
        startCountdown(player1, player2, session);
    }
    
    /**
     * Start the countdown for a duel
     * @param player1 First player
     * @param player2 Second player
     * @param session Duel session
     */
    private void startCountdown(Player player1, Player player2, DuellSession session) {
        int countdownDuration = plugin.getConfigManager().getCountdownDuration();
        
        for (int i = countdownDuration; i > 0; i--) {
            final int seconds = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!session.isActive()) return;
                
                Map<String, String> placeholders = createPlaceholderMap("seconds", String.valueOf(seconds));
                
                // Send countdown message
                if (player1.isOnline()) {
                    plugin.getMessageManager().sendMessage(player1, "duel.countdown", placeholders);
                    plugin.getMessageManager().sendTitle(player1, "countdown", placeholders);
                }
                
                if (player2.isOnline()) {
                    plugin.getMessageManager().sendMessage(player2, "duel.countdown", placeholders);
                    plugin.getMessageManager().sendTitle(player2, "countdown", placeholders);
                }
            }, (countdownDuration - i) * 20L);
        }
        
        // Start the duel after countdown
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!session.isActive()) return;
            
            session.start();
            
            // Send start message
            if (player1.isOnline()) {
                plugin.getMessageManager().sendMessage(player1, "duel.started");
                plugin.getMessageManager().sendTitle(player1, "start", null);
            }
            
            if (player2.isOnline()) {
                plugin.getMessageManager().sendMessage(player2, "duel.started");
                plugin.getMessageManager().sendTitle(player2, "start", null);
            }
        }, countdownDuration * 20L);
    }
    
    /**
     * End a duel
     * @param winnerId UUID of the winner
     * @param loserId UUID of the loser
     */
    public void endDuel(UUID winnerId, UUID loserId) {
        // Get the duel session
        DuellSession session = activeDuels.get(winnerId);
        if (session == null) {
            session = activeDuels.get(loserId);
            if (session == null) return;
        }
        
        // Ensure the session is marked as ended
        if (!session.isActive()) {
            return;
        }
        session.end();
        
        // Remove from active duels
        activeDuels.remove(session.getPlayer1Id());
        activeDuels.remove(session.getPlayer2Id());
        
        // Get players
        UUID actualWinnerId = session.getPlayer1Id().equals(winnerId) ? winnerId : session.getPlayer2Id();
        UUID actualLoserId = session.getPlayer1Id().equals(loserId) ? loserId : session.getPlayer2Id();
        
        Player winner = Bukkit.getPlayer(actualWinnerId);
        Player loser = Bukkit.getPlayer(actualLoserId);
        
        // Handle bet
        double betAmount = session.getBetAmount();
        if (betAmount > 0) {
            plugin.getEconomyManager().transferMoney(actualLoserId, actualWinnerId, betAmount);
        }
        
        // Send messages
        if (winner != null && winner.isOnline()) {
            Map<String, String> placeholders = createPlaceholderMap("player", loser != null ? loser.getName() : "Unknown");
            
            if (betAmount > 0) {
                placeholders.put("amount", String.valueOf(betAmount));
                plugin.getMessageManager().sendMessage(winner, "duel.won-with-bet", placeholders);
            } else {
                plugin.getMessageManager().sendMessage(winner, "duel.won", placeholders);
            }
            
            plugin.getMessageManager().sendTitle(winner, "win", null);
        }
        
        if (loser != null && loser.isOnline()) {
            Map<String, String> placeholders = createPlaceholderMap("player", winner != null ? winner.getName() : "Unknown");
            
            if (betAmount > 0) {
                placeholders.put("amount", String.valueOf(betAmount));
                plugin.getMessageManager().sendMessage(loser, "duel.lost-with-bet", placeholders);
            } else {
                plugin.getMessageManager().sendMessage(loser, "duel.lost", placeholders);
            }
            
            plugin.getMessageManager().sendTitle(loser, "lose", null);
        }
    }
    
    /**
     * Handle a player leaving the server
     * @param playerId UUID of the player
     */
    public void handlePlayerQuit(UUID playerId) {
        // Check if player is in a duel
        DuellSession session = activeDuels.get(playerId);
        if (session != null && session.isActive()) {
            UUID opponentId = session.getOpponentId(playerId);
            
            // End the duel
            endDuel(opponentId, playerId);
            
            // Send message
            Player opponent = Bukkit.getPlayer(opponentId);
            if (opponent != null && opponent.isOnline()) {
                OfflinePlayer quittingPlayer = Bukkit.getOfflinePlayer(playerId);
                plugin.getMessageManager().sendMessage(opponent, "duel.player-left", 
                        createPlaceholderMap("player", quittingPlayer.getName() != null ? quittingPlayer.getName() : "Unknown"));
            }
        }
        
        // Remove pending requests
        Set<DuellRequest> requestsReceived = pendingRequests.remove(playerId);
        if (requestsReceived != null) {
            for (DuellRequest request : requestsReceived) {
                if (requestTimeoutTasks.containsKey(request.getId())) {
                    requestTimeoutTasks.get(request.getId()).cancel();
                    requestTimeoutTasks.remove(request.getId());
                }
                UUID senderId = request.getSenderId();
                if (sentRequests.containsKey(senderId)) {
                    if (sentRequests.get(senderId).remove(request)) {
                        if (sentRequests.get(senderId).isEmpty()) {
                            sentRequests.remove(senderId);
                        }
                    }
                }
            }
        }
        
        // Remove sent requests
        Set<DuellRequest> requestsSent = sentRequests.remove(playerId);
        if (requestsSent != null) {
            for (DuellRequest request : requestsSent) {
                if (requestTimeoutTasks.containsKey(request.getId())) {
                    requestTimeoutTasks.get(request.getId()).cancel();
                    requestTimeoutTasks.remove(request.getId());
                }
                UUID targetId = request.getTargetId();
                if (pendingRequests.containsKey(targetId)) {
                    if (pendingRequests.get(targetId).remove(request)) {
                        if (pendingRequests.get(targetId).isEmpty()) {
                            pendingRequests.remove(targetId);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Check if a player is in a duel
     * @param playerId UUID of the player
     * @return True if the player is in a duel
     */
    public boolean isInDuel(UUID playerId) {
        return activeDuels.containsKey(playerId);
    }
    
    /**
     * Check if a player has a pending request from another player
     * @param targetId UUID of the target player
     * @param senderId UUID of the sender player
     * @return True if there is a pending request
     */
    public boolean hasPendingRequest(UUID targetId, UUID senderId) {
        if (!pendingRequests.containsKey(targetId)) return false;
        
        for (DuellRequest request : pendingRequests.get(targetId)) {
            if (request.getSenderId().equals(senderId)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get a pending request
     * @param targetId UUID of the target player
     * @param senderId UUID of the sender player
     * @return The request, or null if not found
     */
    public DuellRequest getPendingRequest(UUID targetId, UUID senderId) {
        if (!pendingRequests.containsKey(targetId)) return null;
        
        for (DuellRequest request : pendingRequests.get(targetId)) {
            if (request.getSenderId().equals(senderId)) {
                return request;
            }
        }
        
        return null;
    }
    
    /**
     * Check if two players are in the same duel
     * @param player1Id UUID of the first player
     * @param player2Id UUID of the second player
     * @return True if the players are in the same duel
     */
    public boolean areInSameDuel(UUID player1Id, UUID player2Id) {
        DuellSession session1 = activeDuels.get(player1Id);
        DuellSession session2 = activeDuels.get(player2Id);
        
        return session1 != null && session2 != null && session1.equals(session2);
    }
    
    /**
     * Check if a duel is in progress (after countdown)
     * @param playerId UUID of the player
     * @return True if the duel is in progress
     */
    public boolean isDuelInProgress(UUID playerId) {
        DuellSession session = activeDuels.get(playerId);
        return session != null && session.isStarted();
    }

    // Cancel all active duels
    public void cancelAllDuels() {
        Set<UUID> playerIds = new HashSet<>(activeDuels.keySet());
        
        for (UUID playerId : playerIds) {
            DuellSession session = activeDuels.get(playerId);
            if (session != null) {
                session.end();
            }
        }
        
        activeDuels.clear();
        
        // Cancel all timeout tasks
        for (BukkitTask task : requestTimeoutTasks.values()) {
            task.cancel();
        }
        
        requestTimeoutTasks.clear();
        pendingRequests.clear();
        sentRequests.clear();
    }
    
    /**
     * Check if two players are close enough for a duel
     * @param player1 First player
     * @param player2 Second player
     * @return True if the players are close enough
     */
    private boolean isPlayerInRange(Player player1, Player player2) {
        if (!player1.getWorld().equals(player2.getWorld())) {
            return false;
        }
        
        double distance = player1.getLocation().distance(player2.getLocation());
        return distance <= plugin.getConfigManager().getProximityRadius();
    }
    
    /**
     * Create a placeholder map
     * @param key First key
     * @param value First value
     * @return The placeholder map
     */
    private Map<String, String> createPlaceholderMap(String key, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(key, value);
        return placeholders;
    }
    
    /**
     * Create a placeholder map
     * @param key1 First key
     * @param value1 First value
     * @param key2 Second key
     * @param value2 Second value
     * @return The placeholder map
     */
    private Map<String, String> createPlaceholderMap(String key1, String value1, String key2, String value2) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(key1, value1);
        placeholders.put(key2, value2);
        return placeholders;
    }
}
