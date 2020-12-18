package io.keyko.nevermined.exceptions;

public class NftException extends NeverminedException {

    public NftException(String message, Throwable e) {
        super(message, e);
    }

    public NftException(String message) {
        super(message);
    }
}
