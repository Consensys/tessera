package com.quorum.tessera.encryption;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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

    @Test
    public void exceptionIfServiceNotFound() {
        Throwable ex = catchThrowable(() -> EncryptorFactory.newFactory("NOTAVAILABLE"));

        assertThat(ex).isExactlyInstanceOf(EncryptorFactoryNotFoundException.class);
        assertThat(ex).hasMessageContaining("NOTAVAILABLE");
    }
}
