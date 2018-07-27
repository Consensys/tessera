package com.quorum.tessera.server.monitoring;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.management.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MetricsEnquirerTest {
    @Mock
    private MBeanServer mBeanServer;

    private MetricsEnquirer metricsEnquirer;

    private Set<ObjectName> names;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        metricsEnquirer = new MetricsEnquirer(mBeanServer);

        names = new HashSet<>();
    }

    @Test
    public void metricNameDoesNotEndWithTotalSoIsNotIncluded() throws MalformedObjectNameException, IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException, InstanceNotFoundException {
        ObjectName mBeanName = new ObjectName("domain", "key", "value");
        names.add(mBeanName);

        when(mBeanServer.queryNames(new ObjectName("org.glassfish.jersey:type=Tessera,subType=Resources,resource=com.quorum.tessera.api.*,executionTimes=RequestTimes,detail=methods,method=*"), null)).thenReturn(names);

        String attributeName = "name";
        MBeanAttributeInfo[] mBeanAttributes = {new MBeanAttributeInfo(attributeName, "type", "desc", true, false, false)};
        MBeanInfo mBeanInfo = new MBeanInfo(null, null, mBeanAttributes, null, null, null);

        when(mBeanServer.getMBeanInfo(mBeanName)).thenReturn(mBeanInfo);
        when(mBeanServer.getAttribute(any(ObjectName.class), any(String.class))).thenReturn(1);

        List<MBeanMetric> metrics = metricsEnquirer.getMBeanMetrics();

        assertThat(metrics.size()).isEqualTo(0);
    }

    @Test
    public void oneMBeanOneMetric() throws MalformedObjectNameException, IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException, InstanceNotFoundException {
        ObjectName mBeanName = new ObjectName("domain", "key", "value");
        names.add(mBeanName);

        when(mBeanServer.queryNames(new ObjectName("org.glassfish.jersey:type=Tessera,subType=Resources,resource=com.quorum.tessera.api.*,executionTimes=RequestTimes,detail=methods,method=*"), null)).thenReturn(names);

        String attributeName = "name_total";
        MBeanAttributeInfo[] mBeanAttributes = {new MBeanAttributeInfo(attributeName, "type", "desc", true, false, false)};
        MBeanInfo mBeanInfo = new MBeanInfo(null, null, mBeanAttributes, null, null, null);

        when(mBeanServer.getMBeanInfo(mBeanName)).thenReturn(mBeanInfo);
        when(mBeanServer.getAttribute(any(ObjectName.class), any(String.class))).thenReturn(1);

        List<MBeanMetric> metrics = metricsEnquirer.getMBeanMetrics();

        assertThat(metrics.size()).isEqualTo(1);
        assertThat(metrics.get(0).getName()).isEqualTo("name_total");
    }

    @Test
    public void oneMBeanMultipleMetricsSomeNotAddedAsDoNotEndWithTotal() throws MalformedObjectNameException, IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException, InstanceNotFoundException {
        ObjectName mBeanName = new ObjectName("domain", "key", "value");
        names.add(mBeanName);

        when(mBeanServer.queryNames(new ObjectName("org.glassfish.jersey:type=Tessera,subType=Resources,resource=com.quorum.tessera.api.*,executionTimes=RequestTimes,detail=methods,method=*"), null)).thenReturn(names);

        String attributeName = "name_total";
        String attributeName2 = "name2";
        String attributeName3 = "name3_total";
        MBeanAttributeInfo[] mBeanAttributes = {
            new MBeanAttributeInfo(attributeName, "type", "desc", true, false, false),
            new MBeanAttributeInfo(attributeName2, "type", "desc", true, false, false),
            new MBeanAttributeInfo(attributeName3, "type", "desc", true, false, false)
        };
        MBeanInfo mBeanInfo = new MBeanInfo(null, null, mBeanAttributes, null, null, null);

        when(mBeanServer.getMBeanInfo(mBeanName)).thenReturn(mBeanInfo);
        when(mBeanServer.getAttribute(any(ObjectName.class), any(String.class))).thenReturn(1);

        List<MBeanMetric> metrics = metricsEnquirer.getMBeanMetrics();

        assertThat(metrics.size()).isEqualTo(2);
        assertThat(metrics.get(0).getName()).isEqualTo("name_total");
        assertThat(metrics.get(1).getName()).isEqualTo("name3_total");
    }

    @Test
    public void multipleMBeansOneMetricEach() throws MalformedObjectNameException, IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException, InstanceNotFoundException {
        ObjectName mBeanName1 = new ObjectName("domain1", "key1", "value1");
        ObjectName mBeanName2 = new ObjectName("domain2", "key2", "value2");
        names.add(mBeanName1);
        names.add(mBeanName2);

        when(mBeanServer.queryNames(new ObjectName("org.glassfish.jersey:type=Tessera,subType=Resources,resource=com.quorum.tessera.api.*,executionTimes=RequestTimes,detail=methods,method=*"), null)).thenReturn(names);

        String attributeName1 = "name_total";
        String attributeName2 = "name2_total";
        MBeanAttributeInfo[] mBeanAttributes1 = {
            new MBeanAttributeInfo(attributeName1, "type", "desc", true, false, false)
        };

        MBeanAttributeInfo[] mBeanAttributes2 = {
            new MBeanAttributeInfo(attributeName2, "type", "desc", true, false, false)
        };


        MBeanInfo mBeanInfo1 = new MBeanInfo(null, null, mBeanAttributes1, null, null, null);
        MBeanInfo mBeanInfo2 = new MBeanInfo(null, null, mBeanAttributes2, null, null, null);

        when(mBeanServer.getMBeanInfo(mBeanName1)).thenReturn(mBeanInfo1);
        when(mBeanServer.getMBeanInfo(mBeanName2)).thenReturn(mBeanInfo2);
        when(mBeanServer.getAttribute(any(ObjectName.class), any(String.class))).thenReturn(1);

        List<MBeanMetric> metrics = metricsEnquirer.getMBeanMetrics();

        assertThat(metrics.size()).isEqualTo(2);
        assertThat(metrics.get(0).getName()).isEqualTo("name2_total");
        assertThat(metrics.get(1).getName()).isEqualTo("name_total");
    }

    @Test
    public void multipleMBeansSomeMetricsNotAddedAsDoNotEndWithTotal() throws MalformedObjectNameException, IntrospectionException, ReflectionException, AttributeNotFoundException, MBeanException, InstanceNotFoundException {
        ObjectName mBeanName1 = new ObjectName("domain1", "key1", "value1");
        ObjectName mBeanName2 = new ObjectName("domain2", "key2", "value2");
        names.add(mBeanName1);
        names.add(mBeanName2);

        when(mBeanServer.queryNames(new ObjectName("org.glassfish.jersey:type=Tessera,subType=Resources,resource=com.quorum.tessera.api.*,executionTimes=RequestTimes,detail=methods,method=*"), null)).thenReturn(names);

        String attributeName1 = "name_total";
        String attributeName2 = "name2";
        String attributeName3 = "name3_total";
        MBeanAttributeInfo[] mBeanAttributes1 = {
            new MBeanAttributeInfo(attributeName1, "type", "desc", true, false, false)
        };

        MBeanAttributeInfo[] mBeanAttributes2 = {
            new MBeanAttributeInfo(attributeName2, "type", "desc", true, false, false),
            new MBeanAttributeInfo(attributeName3, "type", "desc", true, false, false)
        };


        MBeanInfo mBeanInfo1 = new MBeanInfo(null, null, mBeanAttributes1, null, null, null);
        MBeanInfo mBeanInfo2 = new MBeanInfo(null, null, mBeanAttributes2, null, null, null);

        when(mBeanServer.getMBeanInfo(mBeanName1)).thenReturn(mBeanInfo1);
        when(mBeanServer.getMBeanInfo(mBeanName2)).thenReturn(mBeanInfo2);
        when(mBeanServer.getAttribute(any(ObjectName.class), any(String.class))).thenReturn(1);

        List<MBeanMetric> metrics = metricsEnquirer.getMBeanMetrics();

        assertThat(metrics.size()).isEqualTo(2);
        assertThat(metrics.get(0).getName()).isEqualTo("name3_total");
        assertThat(metrics.get(1).getName()).isEqualTo("name_total");
    }
}
