package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with a not valid configuration of the API
 */
public class InvalidConfiguration extends NevermindException {

    public InvalidConfiguration(String message, Throwable e) {
        super(message, e);
    }

    public InvalidConfiguration(String message) {
        super(message);
    }
}
