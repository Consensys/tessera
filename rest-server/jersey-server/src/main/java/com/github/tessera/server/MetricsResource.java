package com.github.tessera.server;

import javax.management.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.lang.management.ManagementFactory;
import java.util.Set;

@Path("/metrics")
public class MetricsResource {

    @GET
    @Produces("text/plain")
    public String getMetrics() throws MalformedObjectNameException, IntrospectionException, InstanceNotFoundException, ReflectionException, AttributeNotFoundException, MBeanException {

        String response = "";

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

//        Set<ObjectName> mBeanNames = mbs.queryNames(new ObjectName("org.glassfish.jersey:type=Tessera,subType=Uris,resource=*,executionTimes=RequestTimes"), null);
        Set<ObjectName> mBeanNames = mbs.queryNames(new ObjectName("org.glassfish.jersey:type=Tessera,subType=Uris,resource=\"/upcheck\",executionTimes=RequestTimes"), null);

        for(ObjectName mBeanName : mBeanNames) {
            MBeanInfo mBeanInfo = mbs.getMBeanInfo(mBeanName);
            for(MBeanAttributeInfo attributeInfo : mBeanInfo.getAttributes()) {
                response += mBeanName.getKeyProperty("type") + "_" + mBeanName.getKeyProperty("resource").replaceAll("[\"_/]","") +"_" + mBeanName.getKeyProperty("executionTimes") + "_" + attributeInfo.getName().replace("[", "_").replace("]", "") + " " + mbs.getAttribute(mBeanName, attributeInfo.getName()) + "\n";
            }
        }

        return response;
    }
}
