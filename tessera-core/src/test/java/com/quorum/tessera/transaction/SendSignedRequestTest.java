package com.quorum.tessera.transaction;

import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SendSignedRequestTest {

    @Test
    public void build() {
        byte[] signedData = "SignedData".getBytes();
        List<PublicKey> recipients = List.of(mock(PublicKey.class));

        SendSignedRequest request = SendSignedRequest.Builder.create()
            .withSignedData(signedData)
            .withRecipients(recipients)
            .build();

        assertThat(request).isNotNull();
        assertThat(request.getSignedData()).containsExactly(signedData);
        assertThat(request.getRecipients()).hasSize(1).containsAll(recipients);
    }

    @Test(expected = NullPointerException.class)
    public void buidlwithNothing() {
        SendSignedRequest.Builder.create()
            .build();
    }

    @Test(expected = NullPointerException.class)
    public void buidlWithoutSignedData() {
        SendSignedRequest.Builder.create()
            .withRecipients(List.of(mock(PublicKey.class)))
            .build();
    }

    @Test(expected = NullPointerException.class)
    public void buildWithoutRecipients() {
        SendSignedRequest.Builder.create()
            .withSignedData("Data".getBytes())
            .build();
    }
}
