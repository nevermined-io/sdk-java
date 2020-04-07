package io.keyko.ocean.exceptions;

/**
 * Business Exception related with DID Format issues
 */
public class DIDFormatException extends OceanException {

    public DIDFormatException(String message, Throwable e) {
        super(message, e);
    }

    public DIDFormatException(String message) {
        super(message);
    }
}
