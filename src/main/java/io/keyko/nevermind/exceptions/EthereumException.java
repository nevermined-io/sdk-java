package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with Ethereum interactions issues
 */
public class EthereumException extends NevermindException {


    public EthereumException(String message, Throwable e) {
        super(message, e);
    }

    public EthereumException(String message) {
        super(message);
    }
}
