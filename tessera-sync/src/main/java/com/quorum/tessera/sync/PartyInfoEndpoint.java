package com.quorum.tessera.sync;

import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.transaction.TransactionManager;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(
        value = "/sync",
        decoders = {SyncRequestMessageCodec.class},
        encoders = {SyncResponseMessageCodec.class},
        configurator = PartyInfoEndpointConfigurator.class)
public class PartyInfoEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoEndpoint.class);

    private final PartyInfoService partyInfoService;

    private final PayloadEncoder payloadEncoder = PayloadEncoder.create();

    private final TransactionManager transactionManager;

    private final Set<Session> sessionStore = Collections.synchronizedSet(new HashSet<>());

    public PartyInfoEndpoint(PartyInfoService partyInfoService, TransactionManager transactionManager) {
        this.partyInfoService = partyInfoService;
        this.transactionManager = transactionManager;
    }

    @OnOpen
    public void onOpen(Session session) throws IOException, EncodeException {

        PartyInfo partyInfo = partyInfoService.getPartyInfo();

        SyncResponseMessage syncResponseMessage =
                SyncResponseMessage.Builder.create(SyncResponseMessage.Type.PARTY_INFO)
                        .withPartyInfo(partyInfo)
                        .build();

        sessionStore.add(session);

        session.getBasicRemote().sendObject(syncResponseMessage);
    }

    @OnMessage
    public void onSync(Session session, SyncRequestMessage syncRequestMessage) throws IOException, EncodeException {

        LOGGER.info("Session: {}, SyncRequestMessage.type: {}", session.getId(), syncRequestMessage.getType());

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

        PartyInfo existingPartyInfo = partyInfoService.getPartyInfo();

        Optional<PartyInfo> partyInfo = Optional.ofNullable(syncRequestMessage.getPartyInfo());

        SyncResponseMessage.Builder responseBuilder =
                SyncResponseMessage.Builder.create(SyncResponseMessage.Type.PARTY_INFO);

        if (partyInfo.isPresent()) {
            PartyInfo mergedPartyInfo = partyInfoService.updatePartyInfo(partyInfo.get());
            LOGGER.info(
                    "Updated party info for {}", partyInfo.map(PartyInfo::getUrl).orElse("No party info url found "));
            responseBuilder.withPartyInfo(mergedPartyInfo);
        } else {
            LOGGER.info("Adding existing party info to response:  {}", existingPartyInfo.getUrl());
            responseBuilder.withPartyInfo(existingPartyInfo);
        }

        SyncResponseMessage response = responseBuilder.build();

        sessionStore.forEach(
                s -> {
                    WebSocketSessionCallback.execute(
                            () -> {
                                LOGGER.info("Forwarding partyinfo response to session : {}", s.getId());

                                s.getBasicRemote().sendObject(response);

                                LOGGER.info("Sent partyinfo response to session {}", s.getId());
                                return null;
                            });
                });
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        LOGGER.info("Closing {}", session);
        sessionStore.remove(session);
        session.close();
    }

    @OnError
    public void onError(Throwable ex) {
        LOGGER.error("", ex);
    }
}
