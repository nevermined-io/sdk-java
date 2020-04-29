package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with Conditions issues
 */
public class ConditionNotFoundException extends NeverminedException {

    public ConditionNotFoundException(String message, Throwable e) {
        super(message, e);
    }

    public ConditionNotFoundException(String message) {
        super(message);
    }
}
