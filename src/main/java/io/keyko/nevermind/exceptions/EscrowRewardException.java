package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with EscrowReward Fulfill issues
 */
public class EscrowRewardException extends OceanException {
    public EscrowRewardException(String message, Throwable e) {
        super(message, e);
    }

    public EscrowRewardException(String message) {
        super(message);
    }
}
