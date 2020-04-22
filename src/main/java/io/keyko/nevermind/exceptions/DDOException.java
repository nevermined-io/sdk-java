package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with DDOs issues
 */
public class DDOException extends OceanException {

    public DDOException(String message, Throwable e) {
        super(message, e);
    }

    public DDOException(String message) {
        super(message);
    }
}
