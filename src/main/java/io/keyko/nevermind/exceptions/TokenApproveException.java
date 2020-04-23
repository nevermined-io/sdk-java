package io.keyko.nevermind.exceptions;

public class TokenApproveException extends NevermindException {

    public TokenApproveException(String message, Throwable e) {
        super(message, e);
    }

    public TokenApproveException(String message) {
        super(message);
    }
}
