package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with Service issues
 */
public class ServiceException extends NevermindException {

    public ServiceException(String message, Throwable e) {
        super(message, e);
    }

    public ServiceException(String message) {
        super(message);
    }
}
