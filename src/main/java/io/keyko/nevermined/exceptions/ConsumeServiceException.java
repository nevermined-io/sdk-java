package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with issues during the download of a service
 */
public class ConsumeServiceException extends NeverminedException {

    public ConsumeServiceException(String message, Throwable e) {
        super(message, e);
    }

    public ConsumeServiceException(String message) {
        super(message);
    }
}
