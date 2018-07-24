package com.github.tessera.server.monitoring;

import javax.management.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InfluxDbClient {

    private MBeanServer mbs;
    private MBeanServerEnquirerFactory mbsEnquirerFactory;
    private ResponseFormatterFactory formatterFactory;

    public void addMetrics() throws MalformedObjectNameException, IntrospectionException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        mbs = ManagementFactory.getPlatformMBeanServer();

        setMBeanServerEnquirerFactory(new MBeanServerEnquirerFactory());
        setResponseFormatterFactory(new ResponseFormatterFactory());

        MBeanServerEnquirer mbsEnquirer = mbsEnquirerFactory.getMBeanServerEnquirer(mbs);

        Set<ObjectName> mBeanNames;

        mBeanNames = mbsEnquirer.getTesseraResourceMBeanNames();

        ArrayList<MBeanMetric> mBeanMetrics = new ArrayList<>();

        for(ObjectName mBeanName : mBeanNames) {
            List<MBeanMetric> temp = mbsEnquirer.getMetricsForMBean(mBeanName);
            mBeanMetrics.addAll(temp);
        }

        String uri = "localhost:8080";

        InfluxProtocolFormatter formatter = new InfluxProtocolFormatter();
        String formattedMetrics = formatter.format(mBeanMetrics, uri);


        Client client = ClientBuilder.newClient();
        WebTarget myResource = client.target("http://localhost:8086").path("write").queryParam("db", "tessera_demo");
        Response response = myResource.request(MediaType.TEXT_PLAIN).accept(MediaType.TEXT_PLAIN).post(Entity.text(formattedMetrics));

    }

    public void setResponseFormatterFactory(ResponseFormatterFactory factory) {
        this.formatterFactory = factory;
    }

    public void setMBeanServerEnquirerFactory(MBeanServerEnquirerFactory factory) {
        this.mbsEnquirerFactory = factory;
    }
}
