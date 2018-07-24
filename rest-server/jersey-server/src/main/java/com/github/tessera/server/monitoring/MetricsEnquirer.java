package com.github.tessera.server.monitoring;

import javax.management.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MetricsEnquirer {

    private MBeanServer mBeanServer;

    public MetricsEnquirer(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    public List<MBeanMetric> getMBeanMetrics() throws MalformedObjectNameException, IntrospectionException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        ArrayList<MBeanMetric> mBeanMetrics = new ArrayList<>();

        Set<ObjectName> mBeanNames = getTesseraResourceMBeanNames();

        for(ObjectName mBeanName : mBeanNames) {
            List<MBeanMetric> temp = getMetricsForMBean(mBeanName);
            mBeanMetrics.addAll(temp);
        }

        return mBeanMetrics;
    }

    private Set<ObjectName> getTesseraResourceMBeanNames() throws MalformedObjectNameException {
        String pattern = "org.glassfish.jersey:type=Tessera,subType=Resources,resource=com.github.tessera.api.*,executionTimes=RequestTimes,detail=methods,method=*";
        return this.mBeanServer.queryNames(new ObjectName(pattern), null);
    }

    private List<MBeanMetric> getMetricsForMBean(ObjectName mBeanName) throws AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IntrospectionException {
        ArrayList<MBeanMetric> mBeanMetrics = new ArrayList<>();

        MBeanAttributeInfo[] mBeanAttributes = this.mBeanServer.getMBeanInfo(mBeanName).getAttributes();

        for(MBeanAttributeInfo mBeanAttribute : mBeanAttributes) {
            String attributeName = mBeanAttribute.getName();

            if(attributeName.endsWith("total")) {
                String resourceMethod = mBeanName.getKeyProperty("method");
                String value = mBeanServer.getAttribute(mBeanName, attributeName).toString();
                MBeanResourceMetric metric = new MBeanResourceMetric(resourceMethod, attributeName, value);

                mBeanMetrics.add(metric);
            }
        }

        return Collections.unmodifiableList(mBeanMetrics);
    }
}
