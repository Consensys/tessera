package com.quorum.tessera.enclave;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EnclaveFactoryTest {

    private EnclaveFactory enclaveFactory;

    @Before
    public void onSetUp() {
        this.enclaveFactory = EnclaveFactory.create();
    }

    @Test
    public void create() {
        assertThat(enclaveFactory).isNotNull();
    }

}
