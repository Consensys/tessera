package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveException;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.enclave.model.MessageHashFactory;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.NaclException;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.transaction.EncryptedTransactionDAO;
import com.quorum.tessera.transaction.exception.KeyNotFoundException;
import com.quorum.tessera.transaction.model.EncryptedTransaction;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    private final Map<String, Session> sessions = new HashMap<>();

    private final PartyInfoService partyInfoService;

    private final PayloadEncoder payloadEncoder = PayloadEncoder.create();

    private final EncryptedTransactionDAO encryptedTransactionDAO;

    private Enclave enclave;

    public PartyInfoEndpoint(PartyInfoService partyInfoService,EncryptedTransactionDAO encryptedTransactionDAO) {
        this.partyInfoService = partyInfoService;
        this.encryptedTransactionDAO = encryptedTransactionDAO;
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("Open session : {}, {}", session.getId());
        sessions.put(session.getId(), session);
    }

    @OnMessage
    public void onSync(Session session, SyncRequestMessage syncRequestMessage) throws IOException, EncodeException {

        PartyInfo partyInfo = syncRequestMessage.getPartyInfo();

        LOGGER.info("Message {}", partyInfo.getUrl());

        PartyInfo mergedPartyInfo = partyInfoService.updatePartyInfo(partyInfo);

        PublicKey recipientPublicKey = PublicKey.from("".getBytes());

        this.resendTransactions(recipientPublicKey, mergedPartyInfo, session);
    }

    @OnClose
    public void onClose(Session session) {
        partyInfoService.removeRecipient(session.getRequestURI().toString());
        LOGGER.info("Close session: {}", session.getId());
        sessions.remove(session.getId());
    }

    public Collection<Session> getSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    private Optional<PublicKey> searchForRecipientKey(final EncodedPayload payload) {
        for (final PublicKey potentialMatchingKey : enclave.getPublicKeys()) {
            try {
                enclave.unencryptTransaction(payload, potentialMatchingKey);
                return Optional.of(potentialMatchingKey);
            } catch (EnclaveException | IndexOutOfBoundsException | NaclException ex) {
                LOGGER.debug("Attempted payload decryption using wrong key, discarding.");
            }
        }
        return Optional.empty();
    }

    public void resendTransactions(PublicKey recipientPublicKey, PartyInfo mergedPartyInfo, Session session) {

        int offset = 0;
        final int maxResult = 10000;

        long transactionCount = encryptedTransactionDAO.transactionCount();
        while (offset < encryptedTransactionDAO.transactionCount()) {

            encryptedTransactionDAO.retrieveTransactions(offset, maxResult).stream()
                    .map(EncryptedTransaction::getEncodedPayload)
                    .map(payloadEncoder::decode)
                    .filter(
                            payload -> {
                                final boolean isRecipient = payload.getRecipientKeys().contains(recipientPublicKey);
                                final boolean isSender = Objects.equals(payload.getSenderKey(), recipientPublicKey);
                                return isRecipient || isSender;
                            })
                    .forEach(
                            payload -> {
                                final EncodedPayload prunedPayload;

                                if (Objects.equals(payload.getSenderKey(), recipientPublicKey)) {
                                    final PublicKey decryptedKey =
                                            searchForRecipientKey(payload)
                                                    .orElseThrow(
                                                            () -> {
                                                                final MessageHash hash =
                                                                        MessageHashFactory.create()
                                                                                .createFromCipherText(
                                                                                        payload.getCipherText());
                                                                return new KeyNotFoundException(
                                                                        "No key found as recipient of message " + hash);
                                                            });
                                    payload.getRecipientKeys().add(decryptedKey);

                                    // This payload does not need to be pruned as it was not sent by this node and so
                                    // does not contain any other node's data
                                    prunedPayload = payload;
                                } else {
                                    prunedPayload = payloadEncoder.forRecipient(payload, recipientPublicKey);
                                }

                                final SyncResponseMessage syncResponseMessage =
                                        SyncResponseMessage.Builder.create()
                                                .withPartyInfo(mergedPartyInfo)
                                                .withTransactionCount(transactionCount)
                                                .withTransactionOffset(transactionCount)
                                                .withTransactions(payload)
                                                .build();

                                session.getAsyncRemote().sendObject(syncResponseMessage);
                            });
        }
    }
}
