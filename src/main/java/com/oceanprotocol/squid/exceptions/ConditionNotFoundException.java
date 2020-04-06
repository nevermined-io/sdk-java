package com.oceanprotocol.squid.exceptions;

/**
 * Business Exception related with Conditions issues
 */
public class ConditionNotFoundException extends OceanException {

    public ConditionNotFoundException(String message, Throwable e) {
        super(message, e);
    }

    public ConditionNotFoundException(String message) {
        super(message);
    }
}
