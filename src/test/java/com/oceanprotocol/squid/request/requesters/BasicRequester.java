package com.oceanprotocol.squid.request.requesters;

import com.oceanprotocol.squid.request.Requester;
import com.oceanprotocol.squid.request.RequesterConfigurator;
import com.oceanprotocol.squid.request.executors.BasicExecutor;

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
