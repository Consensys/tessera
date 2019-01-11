package com.quorum.tessera.transaction;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.EncodedPayload;
import com.quorum.tessera.encryption.EncodedPayloadWithRecipients;
import com.quorum.tessera.encryption.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.node.PartyInfoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

public class PayloadPublisherTest {

    private static final byte[] EMPTY = new byte[0];

    private static final PublicKey RECIPIENT_KEY = PublicKey.from("RECIPIENT".getBytes());

    private static final EncodedPayload INNER_PAYLOAD
        = new EncodedPayload(PublicKey.from(EMPTY), EMPTY, new Nonce(EMPTY), singletonList(EMPTY), new Nonce(EMPTY));

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

        final EncodedPayloadWithRecipients encodedPayloadWithRecipients
            = new EncodedPayloadWithRecipients(INNER_PAYLOAD, singletonList(RECIPIENT_KEY));

        payloadPublisher.publishPayload(encodedPayloadWithRecipients, RECIPIENT_KEY);

        verify(enclave).getPublicKeys();
    }

    @Test
    public void publishUsingRemoteKey() {

        final String url = "SOMEURL";
        when(partyInfoService.getURLFromRecipientKey(RECIPIENT_KEY)).thenReturn(url);

        final EncodedPayloadWithRecipients encodedPayloadWithRecipients
            = new EncodedPayloadWithRecipients(INNER_PAYLOAD, singletonList(RECIPIENT_KEY));

        byte[] encodedBytes = "encodedBytes".getBytes();
        when(payloadEncoder.encode(any(EncodedPayloadWithRecipients.class))).thenReturn(encodedBytes);
        when(payloadEncoder.forRecipient(encodedPayloadWithRecipients, RECIPIENT_KEY)).thenReturn(encodedPayloadWithRecipients);

        payloadPublisher.publishPayload(encodedPayloadWithRecipients, RECIPIENT_KEY);

        verify(partyInfoService).getURLFromRecipientKey(RECIPIENT_KEY);
        verify(payloadEncoder).encode(any(EncodedPayloadWithRecipients.class));
        verify(payloadEncoder).forRecipient(encodedPayloadWithRecipients, RECIPIENT_KEY);
        verify(p2pClient).push(url, encodedBytes);
        verify(enclave).getPublicKeys();
    }

}
