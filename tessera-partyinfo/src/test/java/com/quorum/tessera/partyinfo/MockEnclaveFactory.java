package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;

import java.util.Optional;

import static org.mockito.Mockito.mock;

public class MockEnclaveFactory implements EnclaveFactory {
    @Override
    public Enclave createLocal(Config config) {
        return mock(Enclave.class);
    }

    @Override
    public Enclave create(Config config) {
        return mock(Enclave.class);
    }

    @Override
    public Optional<Enclave> enclave() {
        return Optional.of(mock(Enclave.class));
    }
}
