package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with LockReward Fulfill issues
 */
public class LockRewardFulfillException extends NevermindException {

    public LockRewardFulfillException(String message, Throwable e) {
        super(message, e);
    }

    public LockRewardFulfillException(String message) {
        super(message);
    }
}
