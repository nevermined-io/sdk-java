package io.keyko.nevermind.request.requesters;

import io.keyko.nevermind.request.executors.BasicExecutor;
import io.keyko.nevermind.request.Requester;
import io.keyko.nevermind.request.RequesterConfigurator;

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
