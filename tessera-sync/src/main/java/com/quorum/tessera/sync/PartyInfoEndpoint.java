package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PartyInfoValidatorCallback;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.partyinfo.ResendRequestType;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
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
    public void onOpen(Session session) {
        sessionStore.add(session);
    }

    @OnMessage
    public void onSync(Session session, SyncRequestMessage syncRequestMessage) throws IOException, EncodeException {

        LOGGER.debug("Session: {}, SyncRequestMessage.type: {}", session.getId(), syncRequestMessage.getType());

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

        PartyInfo partyInfo = Optional.ofNullable(syncRequestMessage.getPartyInfo()).orElse(existingPartyInfo);

        PartyInfoValidatorCallback partyInfoValidatorCallback = new WebsocketPartyInfoValidatorCallback();
        Set<Recipient> recipients =
                partyInfoService.validateAndExtractValidRecipients(partyInfo, partyInfoValidatorCallback);

        if (recipients.isEmpty()) {
            LOGGER.error("No Recipients found for url {}", partyInfo.getUrl());

            throw new SecurityException("No Recipients found for url " + partyInfo.getUrl());
        }

        SyncResponseMessage.Builder responseBuilder =
                SyncResponseMessage.Builder.create(SyncResponseMessage.Type.PARTY_INFO);

        PartyInfo modifiedPartyInfo = new PartyInfo(partyInfo.getUrl(), recipients, partyInfo.getParties());
        PartyInfo updatedPartyInfo = partyInfoService.updatePartyInfo(modifiedPartyInfo);
        responseBuilder.withPartyInfo(updatedPartyInfo);

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
    public void onClose(Session session) {
        sessionStore.remove(session);
    }

    @OnError
    public void onError(Throwable ex) {
        LOGGER.error("", ex);
    }
}
