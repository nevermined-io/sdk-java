package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with issues during the purchaseOrder process
 */
public class OrderException extends NeverminedException {
    public OrderException(String message, Throwable e) {
        super(message, e);
    }

    public OrderException(String message) {
        super(message);
    }
}
