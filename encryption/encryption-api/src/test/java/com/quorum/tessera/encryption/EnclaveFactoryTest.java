package com.quorum.tessera.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
public class EnclaveFactoryTest {

    private EnclaveFactory enclaveFactory;

    @Before
    public void onSetUp() {
        enclaveFactory = EnclaveFactory.create();
    }

    @Test
    public void create() {
        assertThat(enclaveFactory).isNotNull();
    }



}
