package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.enclave.Enclave;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.core.Application;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;

public class Util {

    public static JerseyTest create(Enclave enclave) {

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        return new JerseyTest() {
            @Override
            protected Application configure() {
                
                enable(TestProperties.LOG_TRAFFIC);
                enable(TestProperties.DUMP_ENTITY);
                set(TestProperties.CONTAINER_PORT,"0");

                return ResourceConfig.forApplication(new EnclaveApplication(enclave));

            }
        };
    }



    static class PortUtil {

        private AtomicInteger counter = new AtomicInteger(1024);

        public int nextPort() {

            while (true) {
                int port = counter.getAndIncrement();
                if (isLocalPortFree(port)) {
                    return port;
                }
            }
        }

        private boolean isLocalPortFree(int port) {
            try {
                new ServerSocket(port).close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

}
