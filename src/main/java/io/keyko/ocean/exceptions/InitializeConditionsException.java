package io.keyko.ocean.exceptions;

/**
 * Business Exception related with issues during the Initialization of the ConditionStatusMap of a Service
 */
public class InitializeConditionsException extends OceanException {

    public InitializeConditionsException(String message, Throwable e) {
        super(message, e);
    }

    public InitializeConditionsException(String message) {
        super(message);
    }
}
