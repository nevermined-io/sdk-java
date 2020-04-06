package com.oceanprotocol.squid.request.requesters;

import com.oceanprotocol.squid.request.Requester;
import com.oceanprotocol.squid.request.RequesterConfigurator;
import com.oceanprotocol.squid.request.executors.CreateExecutor;

public class CreateRequester extends Requester {

    public CreateRequester() {

        super(
                new RequesterConfigurator(10, 4, 5000,2, 10000l),
                new CreateExecutor()
        );
    }

    public static void main(String[] args) throws Exception {

        CreateRequester createRequester = new CreateRequester();
        createRequester.run();
    }
}
