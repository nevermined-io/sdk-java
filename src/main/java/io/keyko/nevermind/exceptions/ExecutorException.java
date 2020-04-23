package io.keyko.nevermind.exceptions;

/**
 * Business Exception related with Executor interactions issues
 */
public class ExecutorException extends NevermindException {


    public ExecutorException(String message, Throwable e) {
        super(message, e);
    }

    public ExecutorException(String message) {
        super(message);
    }
}
