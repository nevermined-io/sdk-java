package io.keyko.nevermined.request.requesters;

import io.keyko.nevermined.request.Requester;
import io.keyko.nevermined.request.RequesterConfigurator;
import io.keyko.nevermined.request.executors.BasicExecutor;

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
