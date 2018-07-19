package com.github.tessera.server;

import javax.management.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/metrics")
public class MetricsResource {

    private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    private ResponseFormatter responseFormatter;

    @GET
    @Produces("text/plain")
    public Response getMetrics() {
        HashMap<String, String> metrics = new HashMap<>();

        Set<ObjectName> mBeanNames = null;

        try {
            mBeanNames = mbs.queryNames(new ObjectName("org.glassfish.jersey:type=Tessera,subType=Resources,resource=com.github.tessera.api.*,executionTimes=RequestTimes,detail=methods,method=*"), null);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }

        for(ObjectName mBeanName : mBeanNames) {
            metrics.putAll(getMetricsForMBean(mBeanName));
        }

        return Response.status(Response.Status.OK)
            .header("Content-Type", TEXT_PLAIN)
            .entity(createPlainTextResponse(metrics))
            .build();
    }

    private Map<String, String> getMetricsForMBean(ObjectName mBeanName) {
        HashMap<String, String> mBeanMetrics = new HashMap<>();

        try {
            for(MBeanAttributeInfo mBeanAttributeInfo : mbs.getMBeanInfo(mBeanName).getAttributes()) {
                mBeanMetrics.putAll(getMetricsForAttribute(mBeanName, mBeanAttributeInfo));
            }
        } catch (InstanceNotFoundException | IntrospectionException | ReflectionException e) {
            throw new RuntimeException(e);
        }

        return Collections.unmodifiableMap(mBeanMetrics);
    }

    private Map<String, String> getMetricsForAttribute(ObjectName mBeanName, MBeanAttributeInfo mBeanAttributeInfo) {
        HashMap<String, String> attributeMetrics =  new HashMap<>();

        if(mBeanAttributeInfo.getName().endsWith("total")) {
            try {
                String metricName = mBeanName.getKeyProperty("method") + "_" + mBeanAttributeInfo.getName();
                String metricValue = mbs.getAttribute(mBeanName, mBeanAttributeInfo.getName()).toString();

                attributeMetrics.put(metricName, metricValue);
            } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException e) {
                throw new RuntimeException(e);
            }
        }

        return Collections.unmodifiableMap(attributeMetrics);
    }

    private String createPlainTextResponse(HashMap<String,String> metrics) {
        this.responseFormatter = new PrometheusResponseFormatter();

        return this.responseFormatter.createResponse(metrics);
    }
}
