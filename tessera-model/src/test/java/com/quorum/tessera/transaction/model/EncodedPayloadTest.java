package com.quorum.tessera.transaction.model;

import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.Nonce;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class EncodedPayloadTest {

    @Test
    public void create() {

        final Key senderKey = mock(Key.class);

        final byte[] cipherText = "HELLOW".getBytes();
        final Nonce cipherTextNonce = mock(Nonce.class);
        final List<byte[]> recipientBoxes = Arrays.asList("HELLOW".getBytes());
        Nonce recipientNonce = mock(Nonce.class);

        EncodedPayload sample = new EncodedPayload(senderKey, cipherText, cipherTextNonce, recipientBoxes, recipientNonce);

        assertThat(sample.getSenderKey()).isSameAs(senderKey);
        assertThat(sample.getCipherText()).isEqualTo("HELLOW".getBytes());
        assertThat(sample.getRecipientBoxes()).containsExactly("HELLOW".getBytes());
        assertThat(sample.getRecipientNonce()).isSameAs(recipientNonce);
        assertThat(sample.getCipherTextNonce()).isSameAs(cipherTextNonce);

    }

}
