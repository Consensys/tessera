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

    private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    private ResponseFormatter responseFormatter;

    @GET
    @Produces("text/plain")
    public Response getMetrics() {
        HashMap<String, String> metrics = new HashMap<>();

        MBeanServerEnquirer mbsEnquirer = new MBeanServerEnquirer(mbs);

        Set<ObjectName> mBeanNames;
        try {
            mBeanNames = mbsEnquirer.getTesseraResourceMBeanNames();
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }

        ArrayList<MBeanMetric> mBeanMetrics = new ArrayList<>();

        for(ObjectName mBeanName : mBeanNames) {
            try {
                List<MBeanMetric> temp = mbsEnquirer.getMetricsForMBean(mBeanName);
                mBeanMetrics.addAll(temp);
            } catch (AttributeNotFoundException | MBeanException | InstanceNotFoundException | ReflectionException | IntrospectionException e) {
                throw new RuntimeException(e);
            }
        }

        String plainTextResponse = createPlainTextResponse(mBeanMetrics);

        return Response.status(Response.Status.OK)
            .header("Content-Type", TEXT_PLAIN)
            .entity(plainTextResponse)
            .build();
    }

    private String createPlainTextResponse(ArrayList<MBeanMetric> metrics) {
        this.responseFormatter = new PrometheusResponseFormatter();

        return this.responseFormatter.createResponse(metrics);
    }
}
