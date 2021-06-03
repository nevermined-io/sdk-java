package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with EscrowPaymentCondition Fulfill issues
 */
public class EscrowPaymentException extends NeverminedException {
    public EscrowPaymentException(String message, Throwable e) {
        super(message, e);
    }

    public EscrowPaymentException(String message) {
        super(message);
    }
}
