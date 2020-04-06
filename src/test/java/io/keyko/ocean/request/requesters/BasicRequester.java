package io.keyko.ocean.request.requesters;

import io.keyko.ocean.request.Requester;
import io.keyko.ocean.request.RequesterConfigurator;
import io.keyko.ocean.request.executors.BasicExecutor;

public class BasicRequester extends Requester {


    public BasicRequester() {

        super(
                new RequesterConfigurator(100, 5, 5000,6, 10000l),
                new BasicExecutor()
        );
    }

    public static void main(String[] args) throws Exception {

        BasicRequester basicRequester = new BasicRequester();
        basicRequester.run();
    }

}
