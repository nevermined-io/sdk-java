package io.keyko.nevermined.exceptions;

public class TokenApproveException extends NeverminedException {

    public TokenApproveException(String message, Throwable e) {
        super(message, e);
    }

    public TokenApproveException(String message) {
        super(message);
    }
}
