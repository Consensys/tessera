package com.quorum.tessera.transaction;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.encryption.EncodedPayloadWithRecipients;
import com.quorum.tessera.encryption.EncodedPayload;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class PayloadPublisherTest {

    private PayloadPublisher payloadPublisher;

    private PayloadEncoder payloadEncoder;

    private PartyInfoService partyInfoService;

    private P2pClient p2pClient;

    @Before
    public void onSetUp() {
        payloadEncoder = mock(PayloadEncoder.class);
        partyInfoService = mock(PartyInfoService.class);
        p2pClient = mock(P2pClient.class);

        payloadPublisher = new PayloadPublisherImpl(payloadEncoder, partyInfoService, p2pClient);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder, partyInfoService, p2pClient);
    }

    @Test
    public void publishPayloadPartyOInfoUrlIsEqualToRecipientKeyUrl() {

        String url = "SOMEURL";
        PublicKey recipientKey = mock(PublicKey.class);
        when(partyInfoService.getURLFromRecipientKey(recipientKey)).thenReturn(url);

        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);

        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfo.getUrl()).thenReturn(url);
        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        payloadPublisher.publishPayload(encodedPayloadWithRecipients, recipientKey);

        verify(partyInfoService).getURLFromRecipientKey(recipientKey);
        verify(partyInfoService).getPartyInfo();
    }

    @Test
    public void publishPayloadPartyOInfoUrlIsNotEqualToRecipientKeyUrl() {

        String url = "SOMEURL";
        PublicKey recipientKey = mock(PublicKey.class);
        when(partyInfoService.getURLFromRecipientKey(recipientKey)).thenReturn(url);

        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayloadWithRecipients.getEncodedPayload()).thenReturn(encodedPayload);
        
        when(encodedPayloadWithRecipients.getRecipientKeys()).thenReturn(Collections.singletonList(recipientKey));
        when(encodedPayload.getRecipientBoxes()).thenReturn(Collections.singletonList(new byte[0]));
        when(encodedPayload.getCipherText()).thenReturn(new byte[0]);
        PartyInfo partyInfo = mock(PartyInfo.class);
        
        String otherUrl = "SOMEOTHERURL";
        when(partyInfo.getUrl()).thenReturn(otherUrl);
        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        byte[] encodedBytes = "encodedBytes".getBytes();
        when(payloadEncoder.encode(any(EncodedPayloadWithRecipients.class))).thenReturn(encodedBytes);
        
        payloadPublisher.publishPayload(encodedPayloadWithRecipients, recipientKey);

        verify(partyInfoService).getURLFromRecipientKey(recipientKey);
        verify(partyInfoService).getPartyInfo();
        verify(payloadEncoder).encode(any(EncodedPayloadWithRecipients.class));
        verify(p2pClient).push(url, encodedBytes);
    }

}
