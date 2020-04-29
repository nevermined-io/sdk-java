package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with DID Format issues
 */
public class DIDFormatException extends NeverminedException {

    public DIDFormatException(String message, Throwable e) {
        super(message, e);
    }

    public DIDFormatException(String message) {
        super(message);
    }
}
