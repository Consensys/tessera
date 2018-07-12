package com.github.tessera.api;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.net.MalformedURLException;

@Path("/metrics")
public class MetricsResource {

    JMXServiceURL url;
    JMXConnector jmxc;

    public MetricsResource() {
        String hostName = "localhost";
        String portNum = "8080";

        try {
            url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName + ":" + portNum +  "/jmxrmi");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    @GET
    @Produces("text/plain")
    public String metrics() {

//        try {
//            jmxc = JMXConnectorFactory.connect(url);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return "Hello World";
    }
}
