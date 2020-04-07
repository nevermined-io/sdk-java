package io.keyko.ocean.exceptions;

/**
 * Business Exception related with Ethereum interactions issues
 */
public class EthereumException extends OceanException {


    public EthereumException(String message, Throwable e) {
        super(message, e);
    }

    public EthereumException(String message) {
        super(message);
    }
}
