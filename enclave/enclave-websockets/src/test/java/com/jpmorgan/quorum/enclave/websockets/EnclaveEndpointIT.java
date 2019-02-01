package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.encryption.PublicKey;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.tyrus.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveEndpointIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveEndpointIT.class);

    private Server server;

    private EnclaveAdapter enclaveAdapter;

    @Before
    public void onSetUp() throws Exception {
        server = new Server("localhost", 8025, "/", null, EnclaveEndpoint.class);
        server.start();

        enclaveAdapter = new EnclaveAdapter(URI.create("ws://localhost:8025/enclave"));
        enclaveAdapter.onConstruct();
    }

    @After
    public void onTearDown() {
        enclaveAdapter.onDestroy();
        server.stop();
    }

    @Test
    public void defaultPublicKey() throws Exception {

        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey publicKey = PublicKey.from(Base64.getDecoder().decode(key));

        when(MockEnclaveFactory.ENCLAVE.defaultPublicKey()).thenReturn(publicKey);

        PublicKey result = enclaveAdapter.defaultPublicKey();

        assertThat(result).isEqualTo(publicKey);

    }

    @Test
    public void forwardingKeys() throws Exception {

        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey publicKey = PublicKey.from(Base64.getDecoder().decode(key));

        when(MockEnclaveFactory.ENCLAVE.getForwardingKeys()).thenReturn(Collections.singleton(publicKey));

        Set<PublicKey> results = enclaveAdapter.getForwardingKeys();

        assertThat(results).containsExactly(publicKey);

    }
}
