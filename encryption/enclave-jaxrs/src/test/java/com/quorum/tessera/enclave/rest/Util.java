package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.encryption.Enclave;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

public class Util {

    public static JerseyTest create(Enclave enclave) {
        return new JerseyTest() {
            @Override
            protected Application configure() {

                enable(TestProperties.LOG_TRAFFIC);
                enable(TestProperties.DUMP_ENTITY);

                EnclaveApplication application = new EnclaveApplication(new EnclaveResource(enclave));

                ResourceConfig config = ResourceConfig.forApplication(application);
                config.packages("com.quorum.tessera.enclave.rest");
                return config;
            }

        };
    }
}
