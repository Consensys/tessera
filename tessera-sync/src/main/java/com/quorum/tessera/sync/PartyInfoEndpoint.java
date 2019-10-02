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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Set<Session> sessionStore = ConcurrentHashMap.newKeySet();

    private PartyInfoValidatorCallback partyInfoValidatorCallback = new WebsocketPartyInfoValidatorCallback();

    public PartyInfoEndpoint(PartyInfoService partyInfoService, TransactionManager transactionManager) {
        this.partyInfoService = partyInfoService;
        this.transactionManager = transactionManager;
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.debug("Open session {}", session);
        sessionStore.add(session);
    }

    @OnMessage
    public void onSync(Session session, SyncRequestMessage syncRequestMessage) throws IOException, EncodeException {
        LOGGER.debug("onSync {}", syncRequestMessage);
        if (syncRequestMessage.getType() == SyncRequestMessage.Type.TRANSACTION_PUSH) {
            EncodedPayload payload = syncRequestMessage.getTransactions();
            com.quorum.tessera.data.MessageHash mesageHash =
                    transactionManager.storePayload(payloadEncoder.encode(payload));
            LOGGER.debug("Created message hash {}", mesageHash);

            session.getBasicRemote().sendText(syncRequestMessage.getCorrelationId());
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

            final PartyInfo sendPartyInfo = partyInfo.get();

            boolean noChanges =
                    existingPartyInfo.getRecipients().containsAll(sendPartyInfo.getRecipients())
                            && sendPartyInfo.getRecipients().containsAll(existingPartyInfo.getRecipients());

            if (noChanges) {
                LOGGER.debug("No updates found {}", existingPartyInfo.getUrl());
                return;
            }

            final String url = sendPartyInfo.getUrl();

            // Validate caller and treat no valid certs as security issue.
            final Set<Recipient> recipients =
                    partyInfoService.validateAndExtractValidRecipients(sendPartyInfo, partyInfoValidatorCallback);

            if (recipients.isEmpty()) {
                throw new SecurityException("No key found for url " + url);
            }

            LOGGER.debug("Updating party info {}", sendPartyInfo.getUrl());

            final PartyInfo mergedPartyInfo = partyInfoService.updatePartyInfo(sendPartyInfo);

            LOGGER.debug(
                    "Updated party info for {}",
                    Optional.ofNullable(sendPartyInfo.getUrl()).orElse("No party info url found "));

            responseBuilder.withPartyInfo(mergedPartyInfo);

        } else {
            LOGGER.debug("Adding existing party info to response:  {}", existingPartyInfo.getUrl());
            responseBuilder.withPartyInfo(existingPartyInfo);
        }

        final SyncResponseMessage response = responseBuilder.build();

        sessionStore.stream()
                .filter(Session::isOpen)
                .forEach(
                        s -> {
                            WebSocketSessionCallback.execute(
                                    () -> {
                                        LOGGER.debug("Sending response {}", s);
                                        s.getBasicRemote().sendObject(response);
                                        LOGGER.debug("Sent response {}", s);
                                        return null;
                                    });
                        });
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        LOGGER.trace("Closing {}", session);
        sessionStore.remove(session);
        session.close();
    }

    @OnError
    public void onError(Throwable ex) {
        LOGGER.error("", ex);
    }
}
