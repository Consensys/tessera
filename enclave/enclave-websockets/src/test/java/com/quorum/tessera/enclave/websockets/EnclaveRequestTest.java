package com.quorum.tessera.enclave.websockets;

import org.junit.Test;

public class EnclaveRequestTest {

    @Test(expected = IllegalStateException.class)
    public void validateArgs() {

        EnclaveRequest.Builder.create()
                .withType(EnclaveRequestType.PUBLIC_KEYS)
                .withArg("SOMEARG").build();

    }

    @Test
    public void buildPublicKeys() {

        EnclaveRequest.Builder.create()
                .withType(EnclaveRequestType.PUBLIC_KEYS)
                .build();

    }

}
