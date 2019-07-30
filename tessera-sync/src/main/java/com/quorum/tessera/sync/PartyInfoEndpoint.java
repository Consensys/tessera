package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.partyinfo.ResendRequestType;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.transaction.TransactionManager;
import java.io.IOException;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(
        value = "/sync",
        decoders = {SyncRequestMessageCodec.class, SyncResponseMessageCodec.class},
        encoders = {SyncRequestMessageCodec.class, SyncResponseMessageCodec.class},
        configurator = PartyInfoEndpointConfigurator.class)
public class PartyInfoEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoEndpoint.class);

    private final PartyInfoService partyInfoService;

    private final PayloadEncoder payloadEncoder = PayloadEncoder.create();

    private final TransactionManager transactionManager;

    private final Enclave enclave;

    private final SessionStore sessionStore;

    public PartyInfoEndpoint(
            PartyInfoService partyInfoService,
            TransactionManager transactionManager,
            Enclave enclave,
            SessionStore sessionStore) {
        this.partyInfoService = partyInfoService;
        this.enclave = enclave;
        this.transactionManager = transactionManager;
        this.sessionStore = sessionStore;
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("Open session : {}, {}", session.getId());
        sessionStore.store(session);
    }

    @OnMessage
    public void onSync(Session session, SyncRequestMessage syncRequestMessage) throws IOException, EncodeException {

        if (syncRequestMessage.getType() == SyncRequestMessage.Type.TRANSACTION_PUSH) {
            EncodedPayload payload = syncRequestMessage.getTransactions();
            transactionManager.storePayload(payloadEncoder.encode(payload));
            return;
        }

        if (syncRequestMessage.getType() == SyncRequestMessage.Type.TRANSACTION_SYNC) {

            PublicKey recipientPublicKey = syncRequestMessage.getRecipientKey();

            ResendRequest resendRequest = new ResendRequest();
            resendRequest.setType(ResendRequestType.ALL);
            resendRequest.setPublicKey(recipientPublicKey.encodeToBase64());

            transactionManager.resend(resendRequest);
            return;
        }

        PartyInfo partyInfo = syncRequestMessage.getPartyInfo();

        LOGGER.info("Message {}", partyInfo.getUrl());

        PartyInfo mergedPartyInfo = partyInfoService.updatePartyInfo(partyInfo);

        SyncResponseMessage partyInfoResponseMessage =
                SyncResponseMessage.Builder.create(SyncResponseMessage.Type.PARTY_INFO)
                        .withPartyInfo(mergedPartyInfo)
                        .build();

        session.getBasicRemote().sendObject(partyInfoResponseMessage);
    }

    @OnClose
    public void onClose(Session session) {
        partyInfoService.removeRecipient(session.getRequestURI().toString());
        LOGGER.info("Close session: {}", session.getId());
        sessionStore.remove(session);
    }
}
