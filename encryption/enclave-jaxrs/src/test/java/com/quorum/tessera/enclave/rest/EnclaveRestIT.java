package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.jaxb.UnmarshallerBuilder;
import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import java.io.InputStream;
import java.util.Set;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore
@Configuration
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = EnclaveRestIT.class)
@ImportResource(locations = "classpath:/tessera-enclave-jaxrs-spring.xml")
public class EnclaveRestIT {
    
    
    @Bean
    public Config config() throws JAXBException, XMLStreamException {
        InputStream configFile = getClass().getResourceAsStream("/sample-config.xml");
        return (Config) UnmarshallerBuilder.create()
                .withXmlMediaType()
                .withoutBeanValidation()
                .build()
                .unmarshal(configFile);

    }

    @Inject
    private Enclave enclave;

    private JerseyTest jersey;

    private EnclaveClient enclaveClient;

    @Inject
    private com.quorum.tessera.config.Config config;

    @Before
    public void setUp() throws Exception {

        jersey = Util.create(enclave);
        jersey.setUp();

        enclaveClient = new EnclaveClient(jersey.client(), jersey.target().getUri());
    }

    @After
    public void tearDown() throws Exception {
        jersey.tearDown();

    }

    @Test
    public void defaultPublicKey() {
        PublicKey result = enclaveClient.defaultPublicKey();

        assertThat(result).isNotNull();
        assertThat(result.encodeToBase64())
                .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");

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
        assertThat(result.iterator().next().encodeToBase64())
                .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    }

}
