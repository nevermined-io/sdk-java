package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with issues during the order process
 */
public class OrderException extends NevermindException {
    public OrderException(String message, Throwable e) {
        super(message, e);
    }

    public OrderException(String message) {
        super(message);
    }
}
