package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with DID Format issues
 */
public class DIDFormatException extends NevermindException {

    public DIDFormatException(String message, Throwable e) {
        super(message, e);
    }

    public DIDFormatException(String message) {
        super(message);
    }
}
