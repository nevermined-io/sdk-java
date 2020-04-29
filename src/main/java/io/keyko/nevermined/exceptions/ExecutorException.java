package io.keyko.nevermined.exceptions;

/**
 * Business Exception related with Executor interactions issues
 */
public class ExecutorException extends NeverminedException {


    public ExecutorException(String message, Throwable e) {
        super(message, e);
    }

    public ExecutorException(String message) {
        super(message);
    }
}
