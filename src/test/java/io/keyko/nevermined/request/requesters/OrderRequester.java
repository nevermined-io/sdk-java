package io.keyko.nevermined.request.requesters;

import io.keyko.nevermined.request.Requester;
import io.keyko.nevermined.request.RequesterConfigurator;
import io.keyko.nevermined.request.executors.OrderExecutor;

public class OrderRequester extends Requester {

    public OrderRequester() {

        super(
                new RequesterConfigurator(10, 4, 5000,2, 10000l),
                new OrderExecutor()
        );
    }

    public static void main(String[] args) throws Exception {

        OrderRequester orderRequester = new OrderRequester();
        orderRequester.run();
    }
}
