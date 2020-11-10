package com.quorum.tessera.discovery;

import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EnclaveKeySynchroniserFactoryTest {

    @Ignore
    @Test
    public void provider() {
        EnclaveKeySynchroniser enclaveKeySynchroniser = EnclaveKeySynchroniserFactory.provider();
        assertThat(enclaveKeySynchroniser)
            .isNotNull()
            .isExactlyInstanceOf(EnclaveKeySynchroniserImpl.class);
    }



    @Test
    public void defaultConstructor() {
        EnclaveKeySynchroniserFactory enclaveKeySynchroniserFactory = new EnclaveKeySynchroniserFactory();
        assertThat(enclaveKeySynchroniserFactory).isNotNull();

    }

}
