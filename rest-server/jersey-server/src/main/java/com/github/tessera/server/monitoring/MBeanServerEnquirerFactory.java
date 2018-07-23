package com.github.tessera.server.monitoring;

import javax.management.MBeanServer;

public class MBeanServerEnquirerFactory {
    public MBeanServerEnquirer getMBeanServerEnquirer(MBeanServer mbs) {
        return new MBeanServerEnquirer(mbs);
    }
}
