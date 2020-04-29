package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with issues during the Initialization of the API
 */
public class InitializationException extends NeverminedException {

    public InitializationException(String message, Throwable e) {
        super(message, e);
    }

    public InitializationException(String message) {
        super(message);
    }
}
