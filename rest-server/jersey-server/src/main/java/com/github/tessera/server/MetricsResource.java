package com.github.tessera.server;

import javax.management.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import java.lang.management.ManagementFactory;
import java.util.*;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/metrics")
public class MetricsResource {

    private MBeanServer mbs;
    private MBeanServerEnquirerFactory mbsEnquirerFactory;
    private ResponseFormatterFactory formatterFactory;

    public MetricsResource() {
        mbs = ManagementFactory.getPlatformMBeanServer();
        setMBeanServerEnquirerFactory(new MBeanServerEnquirerFactory());
        setResponseFormatterFactory(new ResponseFormatterFactory());
    }

    @GET
    @Produces("text/plain")
    public Response getMetrics() throws MalformedObjectNameException, IntrospectionException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        MBeanServerEnquirer mbsEnquirer = mbsEnquirerFactory.getMBeanServerEnquirer(mbs);

        Set<ObjectName> mBeanNames;

        mBeanNames = mbsEnquirer.getTesseraResourceMBeanNames();

        ArrayList<MBeanMetric> mBeanMetrics = new ArrayList<>();

        for(ObjectName mBeanName : mBeanNames) {
            List<MBeanMetric> temp = mbsEnquirer.getMetricsForMBean(mBeanName);
            mBeanMetrics.addAll(temp);
        }

        String plainTextResponse = createPlainTextResponse(mBeanMetrics);

        return Response.status(Response.Status.OK)
            .header("Content-Type", TEXT_PLAIN)
            .entity(plainTextResponse)
            .build();
    }

    public void setResponseFormatterFactory(ResponseFormatterFactory factory) {
        this.formatterFactory = factory;
    }

    public void setMBeanServerEnquirerFactory(MBeanServerEnquirerFactory factory) {
        this.mbsEnquirerFactory = factory;
    }

    private String createPlainTextResponse(ArrayList<MBeanMetric> metrics) {
        ResponseFormatter responseFormatter = this.formatterFactory.getResponseFormatter();

        return responseFormatter.createResponse(metrics);
    }
}
