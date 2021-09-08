package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.enclave.Enclave;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Util {

  public static JerseyTest create(Enclave enclave) {

    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    return new JerseyTest() {
      @Override
      protected Application configure() {

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        set(TestProperties.CONTAINER_PORT, "0");

        return ResourceConfig.forApplication(new EnclaveApplication(enclave));
      }
    };
  }
}
