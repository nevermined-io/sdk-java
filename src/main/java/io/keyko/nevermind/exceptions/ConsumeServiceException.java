package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with issues during the consume of a service
 */
public class ConsumeServiceException extends OceanException {

    public ConsumeServiceException(String message, Throwable e) {
        super(message, e);
    }

    public ConsumeServiceException(String message) {
        super(message);
    }
}
