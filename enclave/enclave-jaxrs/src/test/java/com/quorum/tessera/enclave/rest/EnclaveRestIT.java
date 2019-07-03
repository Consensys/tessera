package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.cli.CliDelegate;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.service.Service;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.net.URL;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EnclaveRestIT {

    private Enclave enclave;

    private JerseyTest jersey;

    private RestfulEnclaveClient enclaveClient;

    static {
        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
    }

    @Before
    public void setUp() throws Exception {

        URL url = EnclaveRestIT.class.getResource("/sample-config.json");

        CliResult cliResult = CliDelegate.INSTANCE.execute("-configfile", url.getFile());

        EnclaveFactory enclaveFactory = EnclaveFactory.create();

        Config config = cliResult.getConfig().get();
        this.enclave = enclaveFactory.createLocal(config);

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
        assertThat(enclaveClient.status()).isEqualTo(Service.Status.STARTED);
    }
}
