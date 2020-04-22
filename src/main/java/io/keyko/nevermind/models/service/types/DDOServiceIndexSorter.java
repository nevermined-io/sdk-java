package io.keyko.nevermind.models.service.types;

import io.keyko.nevermind.models.service.Service;

import java.util.Comparator;

public class DDOServiceIndexSorter implements Comparator<Service> {
    @Override
    public int compare(Service service1, Service service2) {
        if (service1.index > service2.index)
            return 1;
        return -1;
    }
}
