package com.quorum.tessera.server.monitoring;

import com.quorum.tessera.config.AppType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.management.*;

public class MetricsEnquirer {

  private MBeanServer mBeanServer;

  public MetricsEnquirer(MBeanServer mBeanServer) {
    this.mBeanServer = mBeanServer;
  }

  public List<MBeanMetric> getMBeanMetrics(AppType appType) {
    List<MBeanMetric> mBeanMetrics = new ArrayList<>();

    Set<ObjectName> mBeanNames;
    try {
      mBeanNames = getTesseraResourceMBeanNames(appType);

      for (ObjectName mBeanName : mBeanNames) {
        List<MBeanMetric> temp;
        try {
          temp = getMetricsForMBean(mBeanName);
        } catch (AttributeNotFoundException
            | MBeanException
            | InstanceNotFoundException
            | ReflectionException
            | IntrospectionException e) {
          throw new RuntimeException(e);
        }
        mBeanMetrics.addAll(temp);
      }

    } catch (MalformedObjectNameException e) {
      throw new RuntimeException(e);
    }

    return Collections.unmodifiableList(mBeanMetrics);
  }

  private Set<ObjectName> getTesseraResourceMBeanNames(AppType appType)
      throws MalformedObjectNameException {
    final String type;
    switch (appType) {
      case P2P:
        type = "P2PRestApp";
        break;
      case Q2T:
        type = "Q2TRestApp";
        break;
      case ADMIN:
        type = "AdminRestApp";
        break;
      case THIRD_PARTY:
        type = "ThirdPartyRestApp";
        break;
      case ENCLAVE:
        type = "EnclaveApplication";
        break;
      default:
        throw new MonitoringNotSupportedException(appType);
    }

    String pattern =
        String.format(
            "org.glassfish.jersey:type=%s,subType=Resources,resource=com.quorum.tessera.*,executionTimes=RequestTimes,detail=methods,method=*",
            type);
    return Collections.unmodifiableSet(this.mBeanServer.queryNames(new ObjectName(pattern), null));
  }

  private List<MBeanMetric> getMetricsForMBean(ObjectName mBeanName)
      throws AttributeNotFoundException, MBeanException, ReflectionException,
          InstanceNotFoundException, IntrospectionException {
    List<MBeanMetric> mBeanMetrics = new ArrayList<>();

    MBeanAttributeInfo[] mBeanAttributes = this.mBeanServer.getMBeanInfo(mBeanName).getAttributes();

    for (MBeanAttributeInfo mBeanAttribute : mBeanAttributes) {
      String attributeName = mBeanAttribute.getName();

      if (attributeName.endsWith("total")) {
        String resourceMethod = mBeanName.getKeyProperty("method");
        String value = mBeanServer.getAttribute(mBeanName, attributeName).toString();
        MBeanResourceMetric metric = new MBeanResourceMetric(resourceMethod, attributeName, value);

        mBeanMetrics.add(metric);
      }
    }

    return Collections.unmodifiableList(mBeanMetrics);
  }
}
