package com.quorum.tessera.encryption;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class EncryptorFactoryTest {

    private EncryptorFactory encryptorFactory;

    @Before
    public void onSetUp() {
        this.encryptorFactory = EncryptorFactory.newFactory("MOCK");

        assertThat(this.encryptorFactory).isExactlyInstanceOf(MockEncryptorFactory.class);
    }

    @Test
    public void create() {
        final Encryptor result = this.encryptorFactory.create();

        assertThat(result).isNotNull().isSameAs(MockEncryptor.INSTANCE);
    }
}
