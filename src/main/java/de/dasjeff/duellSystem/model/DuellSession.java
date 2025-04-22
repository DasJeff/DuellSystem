package de.dasjeff.duellSystem.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an active duel session
 */
public class DuellSession {

    private final UUID id;
    private final UUID player1Id;
    private final UUID player2Id;
    private final double betAmount;
    private final Instant createdAt;
    private Instant startedAt;
    private Instant endedAt;
    private boolean active;
    private boolean started;

    /**
     * Constructor
     * @param player1Id UUID of the first player
     * @param player2Id UUID of the second player
     * @param betAmount Bet amount (0 for friendly duel)
     */
    public DuellSession(UUID player1Id, UUID player2Id, double betAmount) {
        this.id = UUID.randomUUID();
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.betAmount = betAmount;
        this.createdAt = Instant.now();
        this.active = true;
        this.started = false;
    }

    /**
     * Get the session ID
     * @return Session ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Get the first player ID
     * @return First player ID
     */
    public UUID getPlayer1Id() {
        return player1Id;
    }

    /**
     * Get the second player ID
     * @return Second player ID
     */
    public UUID getPlayer2Id() {
        return player2Id;
    }

    /**
     * Get the bet amount
     * @return Bet amount
     */
    public double getBetAmount() {
        return betAmount;
    }

    /**
     * Get the creation time
     * @return Creation time
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Get the start time
     * @return Start time
     */
    public Instant getStartedAt() {
        return startedAt;
    }

    /**
     * Get the end time
     * @return End time
     */
    public Instant getEndedAt() {
        return endedAt;
    }

    /**
     * Check if the session is active
     * @return True if the session is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Check if the duel has started
     * @return True if the duel has started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Start the duel
     */
    public void start() {
        this.startedAt = Instant.now();
        this.started = true;
    }

    /**
     * End the duel
     */
    public void end() {
        this.endedAt = Instant.now();
        this.active = false;
    }

    /**
     * Check if this is a friendly duel (no bet)
     * @return True if this is a friendly duel
     */
    public boolean isFriendly() {
        return betAmount <= 0;
    }

    /**
     * Get the opponent ID
     * @param playerId Player ID
     * @return Opponent ID
     */
    public UUID getOpponentId(UUID playerId) {
        if (playerId.equals(player1Id)) {
            return player2Id;
        } else if (playerId.equals(player2Id)) {
            return player1Id;
        } else {
            return null;
        }
    }

    /**
     * Check if a player is in this duel
     * @param playerId Player ID
     * @return True if the player is in this duel
     */
    public boolean hasPlayer(UUID playerId) {
        return playerId.equals(player1Id) || playerId.equals(player2Id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DuellSession that = (DuellSession) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
