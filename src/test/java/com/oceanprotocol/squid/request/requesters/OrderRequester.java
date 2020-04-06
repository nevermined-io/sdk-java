package com.oceanprotocol.squid.request.requesters;

import com.oceanprotocol.squid.request.Requester;
import com.oceanprotocol.squid.request.RequesterConfigurator;
import com.oceanprotocol.squid.request.executors.OrderExecutor;

public class OrderRequester extends Requester{

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
