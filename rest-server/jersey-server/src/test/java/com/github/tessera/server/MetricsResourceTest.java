package com.github.tessera.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.management.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetricsResourceTest {

    @Mock
    private MBeanServerEnquirerFactory mockEnquirerFactory;

    @Mock
    private MBeanServerEnquirer mockEnquirer;

    @Mock
    private ResponseFormatterFactory mockFormatterFactory;

    @Mock
    private PrometheusResponseFormatter mockFormatter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void metricsCreatedForOneMBean() throws MalformedObjectNameException, IntrospectionException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {

        Set<ObjectName> mBeanNames = new HashSet<>();
        ObjectName mBeanName = new ObjectName("domain:key=value");
        mBeanNames.add(mBeanName);

        List<MBeanMetric> metrics = new ArrayList<>();
        metrics.add(new MBeanResourceMetric("method", "name", "value"));

        when(mockEnquirerFactory.getMBeanServerEnquirer(any(MBeanServer.class))).thenReturn(mockEnquirer);
        when(mockEnquirer.getTesseraResourceMBeanNames()).thenReturn(mBeanNames);
        when(mockEnquirer.getMetricsForMBean(mBeanName)).thenReturn(metrics);

        when(mockFormatterFactory.getResponseFormatter()) .thenReturn(mockFormatter);

        MetricsResource metricsResource = new MetricsResource();
        metricsResource.setMBeanServerEnquirerFactory(mockEnquirerFactory);
        metricsResource.setResponseFormatterFactory(mockFormatterFactory);

        metricsResource.getMetrics();
        verify(mockFormatter).createResponse(metrics);
    }

    @Test
    public void metricsCorrectlyAppendedForMoreThanOneMBean() throws MalformedObjectNameException, IntrospectionException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        Set<ObjectName> mBeanNames = new HashSet<>();
        ObjectName mBeanNameOne = new ObjectName("domain1:key=value");
        ObjectName mBeanNameTwo = new ObjectName("domain2:key=value");
        mBeanNames.add(mBeanNameOne);
        mBeanNames.add(mBeanNameTwo);

        List<MBeanMetric> metricsOne = new ArrayList<>();
        metricsOne.add(new MBeanResourceMetric("method1", "name", "value"));
        List<MBeanMetric> metricsTwo = new ArrayList<>();
        metricsTwo.add(new MBeanResourceMetric("method2", "name", "value"));

        when(mockEnquirerFactory.getMBeanServerEnquirer(any(MBeanServer.class))).thenReturn(mockEnquirer);
        when(mockEnquirer.getTesseraResourceMBeanNames()).thenReturn(mBeanNames);
        when(mockEnquirer.getMetricsForMBean(mBeanNameOne)).thenReturn(metricsOne);
        when(mockEnquirer.getMetricsForMBean(mBeanNameTwo)).thenReturn(metricsTwo);

        when(mockFormatterFactory.getResponseFormatter()) .thenReturn(mockFormatter);

        MetricsResource metricsResource = new MetricsResource();
        metricsResource.setMBeanServerEnquirerFactory(mockEnquirerFactory);
        metricsResource.setResponseFormatterFactory(mockFormatterFactory);

        metricsResource.getMetrics();
        List<MBeanMetric> combinedMetrics = metricsOne;
        combinedMetrics.addAll(metricsTwo);
        verify(mockFormatter).createResponse(combinedMetrics);
    }
}

