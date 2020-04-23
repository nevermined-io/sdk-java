package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with Conditions issues
 */
public class ConditionNotFoundException extends NevermindException {

    public ConditionNotFoundException(String message, Throwable e) {
        super(message, e);
    }

    public ConditionNotFoundException(String message) {
        super(message);
    }
}
