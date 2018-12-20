package com.quorum.tessera.transaction;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.EncodedPayload;
import com.quorum.tessera.encryption.EncodedPayloadWithRecipients;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.node.PartyInfoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

public class PayloadPublisherTest {

    private static final byte[] EMPTY = new byte[0];

    private PayloadPublisher payloadPublisher;

    private PayloadEncoder payloadEncoder;

    private PartyInfoService partyInfoService;

    private P2pClient p2pClient;

    private Enclave enclave;

    @Before
    public void onSetUp() {
        payloadEncoder = mock(PayloadEncoder.class);
        partyInfoService = mock(PartyInfoService.class);
        p2pClient = mock(P2pClient.class);
        enclave = mock(Enclave.class);

        when(enclave.getPublicKeys()).thenReturn(Collections.emptySet());

        payloadPublisher = new PayloadPublisherImpl(payloadEncoder, partyInfoService, p2pClient, enclave);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder, partyInfoService, p2pClient, enclave);
    }

    @Test
    public void publishUsingOwnKey() {

        PublicKey recipientKey = mock(PublicKey.class);

        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(recipientKey));

        final EncodedPayload encodedPayload = new EncodedPayload(
            PublicKey.from(EMPTY), EMPTY, new Nonce(EMPTY), singletonList(EMPTY), new Nonce(EMPTY)
        );

        final EncodedPayloadWithRecipients encodedPayloadWithRecipients = new EncodedPayloadWithRecipients(
            encodedPayload, singletonList(recipientKey)
        );

        payloadPublisher.publishPayload(encodedPayloadWithRecipients, recipientKey);

        verify(enclave).getPublicKeys();
    }

    @Test
    public void publishUsingRemoteKey() {

        final String url = "SOMEURL";
        PublicKey recipientKey = mock(PublicKey.class);
        when(partyInfoService.getURLFromRecipientKey(recipientKey)).thenReturn(url);

        final EncodedPayload encodedPayload = new EncodedPayload(
            PublicKey.from(EMPTY), EMPTY, new Nonce(EMPTY), singletonList(EMPTY), new Nonce(EMPTY)
        );

        final EncodedPayloadWithRecipients encodedPayloadWithRecipients = new EncodedPayloadWithRecipients(
            encodedPayload, singletonList(recipientKey)
        );

        byte[] encodedBytes = "encodedBytes".getBytes();
        when(payloadEncoder.encode(any(EncodedPayloadWithRecipients.class))).thenReturn(encodedBytes);

        payloadPublisher.publishPayload(encodedPayloadWithRecipients, recipientKey);

        verify(partyInfoService).getURLFromRecipientKey(recipientKey);
        verify(payloadEncoder).encode(any(EncodedPayloadWithRecipients.class));
        verify(p2pClient).push(URI.create(url), encodedBytes);
        verify(enclave).getPublicKeys();
    }

}
