package com.quorum.tessera.encryption;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class EncryptorFactoryTest {

    private EncryptorFactory facadeFactory;

    @Before
    public void onSetUp() {
        this.facadeFactory = EncryptorFactory.newFactory();

        assertThat(this.facadeFactory).isExactlyInstanceOf(MockEncryptorFactory.class);
    }

    @Test
    public void create() {
        final Encryptor result = this.facadeFactory.create();

        assertThat(result).isNotNull().isSameAs(MockEncryptor.INSTANCE);
    }

}
