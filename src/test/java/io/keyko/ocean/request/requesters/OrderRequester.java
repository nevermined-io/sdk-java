package io.keyko.ocean.request.requesters;

import io.keyko.ocean.request.Requester;
import io.keyko.ocean.request.RequesterConfigurator;
import io.keyko.ocean.request.executors.OrderExecutor;

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
