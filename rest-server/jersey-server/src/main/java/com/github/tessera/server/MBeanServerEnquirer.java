package com.github.tessera.server;

import javax.management.*;
import java.util.*;

public class MBeanServerEnquirer {
    private MBeanServer mBeanServer;

    public MBeanServerEnquirer(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    public Set<ObjectName> getTesseraResourceMBeanNames() throws MalformedObjectNameException {
        String pattern = "org.glassfish.jersey:type=Tessera,subType=Resources,resource=com.github.tessera.api.*,executionTimes=RequestTimes,detail=methods,method=*";
        return this.mBeanServer.queryNames(new ObjectName(pattern), null);
    }

    public List<MBeanMetric> getMetricsForMBean(ObjectName mBeanName) throws AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IntrospectionException {
        ArrayList<MBeanMetric> mBeanMetrics = new ArrayList<>();

        MBeanAttributeInfo[] mBeanAttributes = this.mBeanServer.getMBeanInfo(mBeanName).getAttributes();

        for(MBeanAttributeInfo mBeanAttribute : mBeanAttributes) {
            String attributeName = mBeanAttribute.getName();

            if(attributeName.endsWith("total")) {
                MBeanResourceMetric metric = new MBeanResourceMetric();
                metric.setResourceMethod(mBeanName.getKeyProperty("method"));
                metric.setName(attributeName);
                metric.setValue(mBeanServer.getAttribute(mBeanName, attributeName));
                mBeanMetrics.add(metric);
            }
        }

        return Collections.unmodifiableList(mBeanMetrics);
    }
}
