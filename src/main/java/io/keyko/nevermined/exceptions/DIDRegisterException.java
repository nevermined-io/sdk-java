package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with DID issues during the Register process
 */
public class DIDRegisterException extends NeverminedException {

    public DIDRegisterException(String message, Throwable e) {
        super(message, e);
    }

    public DIDRegisterException(String message) {
        super(message);
    }
}
