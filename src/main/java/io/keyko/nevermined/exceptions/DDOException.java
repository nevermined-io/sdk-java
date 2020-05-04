package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with DDOs issues
 */
public class DDOException extends NeverminedException {

    public DDOException(String message, Throwable e) {
        super(message, e);
    }

    public DDOException(String message) {
        super(message);
    }
}
