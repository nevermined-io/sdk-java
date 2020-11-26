package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with Provenance issues
 */
public class ProvenanceException extends NeverminedException {

    public ProvenanceException(String message, Throwable e) {
        super(message, e);
    }

    public ProvenanceException(String message) {
        super(message);
    }
}
