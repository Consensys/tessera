package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.partyinfo.ResendRequestType;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;

import org.junit.Test;
import static org.mockito.Mockito.*;

public class PartyInfoEndpointTest {

    private PartyInfoEndpoint partyInfoEndpoint;

    private Session session;

    private PartyInfoService partyInfoService;

    private TransactionManager transactionManager;

    @Before
    public void onSetUp() {

        partyInfoService = mock(PartyInfoService.class);

        transactionManager = mock(TransactionManager.class);

        partyInfoEndpoint = new PartyInfoEndpoint(partyInfoService, transactionManager);
        session = mock(Session.class);
        when(session.getId()).thenReturn(UUID.randomUUID().toString());
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService, transactionManager);
    }

    @Test
    public void onSyncPartyInfoNoUpdates() throws Exception {

        PartyInfo partyInfo = Fixtures.samplePartyInfo();

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);
        when(partyInfoService.updatePartyInfo(partyInfo)).thenReturn(partyInfo);

        SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO).withPartyInfo(partyInfo).build();

        partyInfoEndpoint.onSync(session, syncRequestMessage);
        partyInfoEndpoint.onClose(session);

        verify(partyInfoService).getPartyInfo();
    }

    @Test
    public void onSyncPartyInfoWithUpdatesInMessage() throws Exception {

        PartyInfo partyInfo = Fixtures.samplePartyInfo();

        PartyInfo existingParrtyInfo =
                new PartyInfo(partyInfo.getUrl(), Collections.emptySet(), Collections.emptySet());
        when(partyInfoService.getPartyInfo()).thenReturn(existingParrtyInfo);
        when(partyInfoService.updatePartyInfo(partyInfo)).thenReturn(partyInfo);

        Session otherClientSession = mock(Session.class);
        Basic basic = mock(Basic.class);
        when(otherClientSession.getBasicRemote()).thenReturn(basic);
        when(otherClientSession.isOpen()).thenReturn(true);

        partyInfoEndpoint.onOpen(otherClientSession);

        final SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO).withPartyInfo(partyInfo).build();

        partyInfoEndpoint.onSync(session, syncRequestMessage);
        partyInfoEndpoint.onClose(session);

        verify(partyInfoService).getPartyInfo();
        verify(partyInfoService).updatePartyInfo(partyInfo);
        verify(basic).sendObject(any());
    }

    @Test
    public void onSyncTransactions() throws Exception {

        PublicKey recipientKey = Fixtures.sampleKey();

        SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.TRANSACTION_SYNC)
                        .withRecipientKey(recipientKey)
                        .build();

        List<ResendRequest> requests = new ArrayList<>();
        doAnswer(
                        (iom) -> {
                            requests.add(iom.getArgument(0));
                            return null;
                        })
                .when(transactionManager)
                .resend(any(ResendRequest.class));

        partyInfoEndpoint.onSync(session, syncRequestMessage);

        assertThat(requests).hasSize(1);

        ResendRequest result = requests.get(0);
        assertThat(result.getType()).isEqualTo(ResendRequestType.ALL);
        assertThat(result.getPublicKey()).isEqualTo(recipientKey.encodeToBase64());

        verify(transactionManager).resend(any(ResendRequest.class));
    }

    @Test
    public void onSyncTransactionPush() throws Exception {

        EncodedPayload transactions = Fixtures.samplePayload();

        SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.TRANSACTION_PUSH)
                        .withTransactions(transactions)
                        .build();

        partyInfoEndpoint.onSync(session, syncRequestMessage);

        byte[] expectedData = PayloadEncoder.create().encode(transactions);
        verify(transactionManager).storePayload(expectedData);
    }

    @Test
    public void onError() {
        partyInfoEndpoint.onError(new Exception("Ouch"));
    }

    @Test
    public void onSyncPartyInfoNoInfoProvided() throws Exception {

        PartyInfo partyInfo = Fixtures.samplePartyInfo();

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        final SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO).build();

        partyInfoEndpoint.onSync(session, syncRequestMessage);
        partyInfoEndpoint.onClose(session);

        verify(partyInfoService).getPartyInfo();
    }
}
