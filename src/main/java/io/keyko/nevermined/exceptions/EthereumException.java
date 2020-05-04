package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with Ethereum interactions issues
 */
public class EthereumException extends NeverminedException {


    public EthereumException(String message, Throwable e) {
        super(message, e);
    }

    public EthereumException(String message) {
        super(message);
    }
}
