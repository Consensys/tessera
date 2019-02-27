package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.cli.CliDelegate;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveImpl;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.service.Service;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.net.URL;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Configuration
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = EnclaveRestIT.class)
@ImportResource(locations = "classpath:/tessera-enclave-jaxrs-spring.xml")
public class EnclaveRestIT {

    @BeforeClass
    public static void onClass() throws Exception {
        URL url = EnclaveRestIT.class.getResource("/sample-config.json");
        CliDelegate.INSTANCE.execute("-configfile", url.getFile());
    }

    @Inject
    private Enclave enclave;

    private JerseyTest jersey;

    private RestfulEnclaveClient enclaveClient;

    @Before
    public void setUp() throws Exception {
        assertThat(enclave).isInstanceOf(EnclaveImpl.class);
        jersey = Util.create(enclave);
        jersey.setUp();

        enclaveClient = new RestfulEnclaveClient(jersey.client(), jersey.target().getUri());
    }

    @After
    public void tearDown() throws Exception {
        jersey.tearDown();
    }

    @Test
    public void defaultPublicKey() {
        PublicKey result = enclaveClient.defaultPublicKey();

        assertThat(result).isNotNull();
        assertThat(result.encodeToBase64()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    }

    @Test
    public void forwardingKeys() {
        Set<PublicKey> result = enclaveClient.getForwardingKeys();

        assertThat(result).isEmpty();
    }

    @Test
    public void getPublicKeys() {
        Set<PublicKey> result = enclaveClient.getPublicKeys();

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().encodeToBase64()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    }
    
    @Test
    public void status() {
        assertThat(enclaveClient.status())
                .isEqualTo(Service.Status.STARTED);
        
    }

}
