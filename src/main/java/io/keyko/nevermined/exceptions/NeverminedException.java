package io.keyko.nevermined.exceptions;

/**
 * Base Class to implement a hierarchy of Functional Ocean's Exceptions
 */
public abstract class NeverminedException extends Exception {

    public NeverminedException(String message, Throwable e) {
        super(message, e);
    }

    public NeverminedException(String message) {
        super(message);
    }
}
