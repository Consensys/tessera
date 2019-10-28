package com.jpmorgan.quorum.encryption.aec;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.quorum.tessera.encryption.Encryptor;

public class AecEncryptorFactoryTest {

    private AecEncryptorFactory encryptorFactory;

    @Before
    public void setUp() {
        this.encryptorFactory = new AecEncryptorFactory();
    }

    @Test
    public void createInstance() {
        final Encryptor result = encryptorFactory.create();
        assertThat(encryptorFactory.getType()).isEqualTo("AEC");
        assertThat(result).isNotNull().isExactlyInstanceOf(AecEncryptor.class);
    }
}
