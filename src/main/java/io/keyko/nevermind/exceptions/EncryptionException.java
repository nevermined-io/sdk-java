package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with Encrypt/Decrypt issues
 */
public class EncryptionException extends OceanException {

    public EncryptionException(String message, Throwable e) {
        super(message, e);
    }

    public EncryptionException(String message) {
        super(message);
    }
}
