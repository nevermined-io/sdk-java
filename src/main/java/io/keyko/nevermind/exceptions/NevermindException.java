package io.keyko.nevermind.exceptions;

/**
 * Base Class to implement a hierarchy of Functional Ocean's Exceptions
 */
public abstract class NevermindException extends Exception {

    public NevermindException(String message, Throwable e) {
        super(message, e);
    }

    public NevermindException(String message) {
        super(message);
    }
}
