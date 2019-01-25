package com.quorum.tessera.transaction;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.transaction.exception.PublishPayloadException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class PayloadPublisherTest {

    private static final byte[] EMPTY = new byte[0];

    private static final PublicKey RECIPIENT_KEY = PublicKey.from("RECIPIENT".getBytes());

    private PayloadPublisher payloadPublisher;

    private PayloadEncoder payloadEncoder;

    private PartyInfoService partyInfoService;

    private P2pClient p2pClient;

    private Enclave enclave;

    @Before
    public void onSetUp() {
        this.payloadEncoder = mock(PayloadEncoder.class);
        this.partyInfoService = mock(PartyInfoService.class);
        this.p2pClient = mock(P2pClient.class);
        this.enclave = mock(Enclave.class);

        when(enclave.getPublicKeys()).thenReturn(Collections.emptySet());

        this.payloadPublisher = new PayloadPublisherImpl(payloadEncoder, partyInfoService, p2pClient, enclave);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder, partyInfoService, p2pClient, enclave);
    }

    @Test
    public void publishUsingOwnKey() {

        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(RECIPIENT_KEY));

        final EncodedPayload payload = new EncodedPayload(
            PublicKey.from(EMPTY), EMPTY, new Nonce(EMPTY),
            singletonList(EMPTY), new Nonce(EMPTY), singletonList(RECIPIENT_KEY)
        );

        payloadPublisher.publishPayload(payload, RECIPIENT_KEY);

        verify(enclave).getPublicKeys();
    }

    @Test
    public void publishUsingRemoteKey() {

        final String url = "SOMEURL";
        when(partyInfoService.getURLFromRecipientKey(RECIPIENT_KEY)).thenReturn(url);

        final EncodedPayload payload = new EncodedPayload(
            PublicKey.from(EMPTY), EMPTY, new Nonce(EMPTY),
            singletonList(EMPTY), new Nonce(EMPTY), singletonList(RECIPIENT_KEY)
        );

        byte[] encodedBytes = "encodedBytes".getBytes();
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn(encodedBytes);

        when(p2pClient.push(url, encodedBytes)).thenReturn("response".getBytes());

        payloadPublisher.publishPayload(payload, RECIPIENT_KEY);

        verify(partyInfoService).getURLFromRecipientKey(RECIPIENT_KEY);
        verify(payloadEncoder).encode(any(EncodedPayload.class));
        verify(p2pClient).push(url, encodedBytes);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void publishToTargetUnsuccessfulThrowsException() {
        final String url = "SOMEURL";
        when(partyInfoService.getURLFromRecipientKey(RECIPIENT_KEY)).thenReturn(url);

        final EncodedPayload payload = new EncodedPayload(
            PublicKey.from(EMPTY), EMPTY, new Nonce(EMPTY),
            singletonList(EMPTY), new Nonce(EMPTY), singletonList(RECIPIENT_KEY)
        );

        byte[] encodedBytes = "encodedBytes".getBytes();
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn(encodedBytes);
        when(payloadEncoder.forRecipient(payload, RECIPIENT_KEY)).thenReturn(payload);

        when(p2pClient.push(url, encodedBytes)).thenReturn(null);

        Throwable ex = catchThrowable(() -> payloadPublisher.publishPayload(payload, RECIPIENT_KEY));

        assertThat(ex).isExactlyInstanceOf(PublishPayloadException.class);
        assertThat(ex.getMessage()).isEqualTo("Unable to push payload to recipient " + RECIPIENT_KEY.encodeToBase64());

        verify(partyInfoService).getURLFromRecipientKey(RECIPIENT_KEY);
        verify(payloadEncoder).encode(any(EncodedPayload.class));
        verify(p2pClient).push(url, encodedBytes);
        verify(enclave).getPublicKeys();
    }

}
