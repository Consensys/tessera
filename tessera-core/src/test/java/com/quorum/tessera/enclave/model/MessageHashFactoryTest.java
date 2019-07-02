package com.quorum.tessera.enclave.model;

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

public class MessageHashFactoryTest {

    @Test
    public void createFromCiperText() {
        MessageHashFactory messageHashFactory = new MessageHashFactory() {};
        String cipherText = "cipherText";
        MessageHash messageHash = messageHashFactory.createFromCipherText(cipherText.getBytes());

        assertThat(messageHash).isNotNull();
    }
}
