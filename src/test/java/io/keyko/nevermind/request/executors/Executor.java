package io.keyko.nevermind.request.executors;

public interface Executor {

    public void setUp();

    public Boolean executeRequest() throws Exception;
}
