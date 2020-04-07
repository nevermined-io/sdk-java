package io.keyko.ocean.exceptions;

/**
 * Business Exception related with LockReward Fulfill issues
 */
public class LockRewardFulfillException extends OceanException {

    public LockRewardFulfillException(String message, Throwable e) {
        super(message, e);
    }

    public LockRewardFulfillException(String message) {
        super(message);
    }
}
