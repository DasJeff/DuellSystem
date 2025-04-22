package de.dasjeff.duellSystem.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a duel request
 */
public class DuellRequest {

    private final UUID id;
    private final UUID senderId;
    private final UUID targetId;
    private final double betAmount;
    private final Instant createdAt;

    /**
     * Constructor
     * @param senderId UUID of the player sending the request
     * @param targetId UUID of the player receiving the request
     * @param betAmount Bet amount (0 for friendly duel)
     */
    public DuellRequest(UUID senderId, UUID targetId, double betAmount) {
        this.id = UUID.randomUUID();
        this.senderId = senderId;
        this.targetId = targetId;
        this.betAmount = betAmount;
        this.createdAt = Instant.now();
    }

    /**
     * Get the request ID
     * @return Request ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Get the sender ID
     * @return Sender ID
     */
    public UUID getSenderId() {
        return senderId;
    }

    /**
     * Get the target ID
     * @return Target ID
     */
    public UUID getTargetId() {
        return targetId;
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
     * Check if this is a friendly duel (no bet)
     * @return True if this is a friendly duel
     */
    public boolean isFriendly() {
        return betAmount <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DuellRequest that = (DuellRequest) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
