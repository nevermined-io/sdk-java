package io.keyko.nevermind.request.requesters;

import io.keyko.nevermind.request.executors.CreateExecutor;
import io.keyko.nevermind.request.Requester;
import io.keyko.nevermind.request.RequesterConfigurator;

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
