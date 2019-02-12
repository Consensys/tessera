package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;


public class EncodedPayloadBuilderTest {
    
    @Test
    public void build() {
        final PublicKey senderKey = mock(PublicKey.class);

        final byte[] cipherText = "cipherText".getBytes();
        final byte[] cipherTextNonce = "cipherTextNonce".getBytes();

        final byte[] recipientNonce = "recipientNonce".getBytes();
        final List<PublicKey> recipientList = new ArrayList<>();

        final byte[] recipientBox = "recipientBox".getBytes();
        
        final EncodedPayload sample
            = EncodedPayloadBuilder.create()
                    .withSenderKey(senderKey)
                    .withCipherText(cipherText)
                    .withCipherTextNonce(cipherTextNonce)
                    .withRecipientBoxes(Arrays.asList(recipientBox))
                    .withRecipientNonce(recipientNonce)
                    .withRecipientKeys(recipientList.toArray(new PublicKey[0]))
                    .build();

        
        
        
        assertThat(sample.getSenderKey()).isSameAs(senderKey);
        assertThat(sample.getCipherText()).isEqualTo("cipherText".getBytes());
        
        assertThat(sample.getRecipientBoxes()).hasSize(1);
        
        assertThat(sample.getRecipientNonce().getNonceBytes()).isEqualTo(recipientNonce);
        assertThat(sample.getCipherTextNonce().getNonceBytes()).isEqualTo(cipherTextNonce);
        assertThat(sample.getRecipientKeys()).isEqualTo(recipientList);
    }
    
}
