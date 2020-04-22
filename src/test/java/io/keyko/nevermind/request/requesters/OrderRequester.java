package io.keyko.nevermind.request.requesters;

import io.keyko.nevermind.request.executors.OrderExecutor;
import io.keyko.nevermind.request.Requester;
import io.keyko.nevermind.request.RequesterConfigurator;

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
