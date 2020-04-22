package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with issues during the Initialization of the API
 */
public class InitializationException extends OceanException {

    public InitializationException(String message, Throwable e) {
        super(message, e);
    }

    public InitializationException(String message) {
        super(message);
    }
}
