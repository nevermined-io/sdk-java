package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with EscrowReward Fulfill issues
 */
public class EscrowRewardException extends NeverminedException {
    public EscrowRewardException(String message, Throwable e) {
        super(message, e);
    }

    public EscrowRewardException(String message) {
        super(message);
    }
}
