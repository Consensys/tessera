package com.quorum.tessera.data;

import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.data.MessageHash;
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

    @Test
    public void create() {
        assertThat(MessageHashFactory.create()).isNotNull();
    }
}
