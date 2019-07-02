package com.quorum.tessera.enclave;

import com.quorum.tessera.config.Config;
import static org.mockito.Mockito.mock;

public class MockEnclaveClientFactory implements EnclaveClientFactory {

    @Override
    public EnclaveClient create(Config config) {
        return mock(EnclaveClient.class);
    }
}
