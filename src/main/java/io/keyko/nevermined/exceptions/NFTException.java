package io.keyko.nevermined.exceptions;

public class NFTException extends NeverminedException {

    public NFTException(String message, Throwable e) {
        super(message, e);
    }

    public NFTException(String message) {
        super(message);
    }
}
