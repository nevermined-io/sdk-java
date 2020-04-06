package com.oceanprotocol.squid.request.executors;

public interface Executor {

    public void setUp();

    public Boolean executeRequest() throws Exception;
}
