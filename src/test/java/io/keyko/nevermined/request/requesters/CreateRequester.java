package io.keyko.nevermined.request.requesters;

import io.keyko.nevermined.request.Requester;
import io.keyko.nevermined.request.RequesterConfigurator;
import io.keyko.nevermined.request.executors.CreateExecutor;

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
