package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with Service issues
 */
public class ServiceException extends NeverminedException {

    public ServiceException(String message, Throwable e) {
        super(message, e);
    }

    public ServiceException(String message) {
        super(message);
    }
}
