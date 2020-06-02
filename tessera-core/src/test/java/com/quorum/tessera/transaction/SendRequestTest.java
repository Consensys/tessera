package com.quorum.tessera.transaction;

import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SendRequestTest {

    @Test
    public void buildWithEverything() {

        byte[] payload = "Payload".getBytes();
        PublicKey sender = mock(PublicKey.class);
        List<PublicKey> recipients = List.of(mock(PublicKey.class));

        SendRequest sendRequest = SendRequest.Builder.create()
            .withPayload(payload)
            .withSender(sender)
            .withRecipients(recipients)
            .build();

        assertThat(sendRequest).isNotNull();
        assertThat(sendRequest.getSender()).isSameAs(sender);
        assertThat(sendRequest.getPayload()).containsExactly(payload);
        assertThat(sendRequest.getRecipients()).containsAll(recipients);

    }

    @Test(expected = NullPointerException.class)
    public void buildWithNothing() {

        SendRequest.Builder.create()
            .build();

    }

    @Test(expected = NullPointerException.class)
    public void buildWithoutPayload() {

        PublicKey sender = mock(PublicKey.class);
        List<PublicKey> recipients = List.of(mock(PublicKey.class));

        SendRequest.Builder.create()
            .withSender(sender)
            .withRecipients(recipients)
            .build();

    }

    @Test(expected = NullPointerException.class)
    public void buildWithoutSender() {

        byte[] payload = "Payload".getBytes();

        List<PublicKey> recipients = List.of(mock(PublicKey.class));

        SendRequest.Builder.create()
            .withPayload(payload)
            .withRecipients(recipients)
            .build();

    }

    @Test(expected = NullPointerException.class)
    public void buildWithoutRecipients() {

        byte[] payload = "Payload".getBytes();
        PublicKey sender = mock(PublicKey.class);

        SendRequest.Builder.create()
            .withPayload(payload)
            .withSender(sender)
            .build();


    }
}
