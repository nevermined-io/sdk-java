package io.keyko.ocean.exceptions;

/**
 * Base Class to implement a hierarchy of Functional Ocean's Exceptions
 */
public abstract class OceanException extends Exception {

    public OceanException(String message, Throwable e) {
        super(message, e);
    }

    public OceanException(String message) {
        super(message);
    }
}
