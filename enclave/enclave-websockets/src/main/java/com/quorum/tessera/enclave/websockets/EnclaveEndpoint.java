package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.service.Service.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

@ServerEndpoint(value = "/enclave",
        encoders = {EnclaveResponseCodec.class},
        decoders = {EnclaveRequestCodec.class})
public class EnclaveEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveEndpoint.class);

    private final ThreadLocal<Enclave> enclaveThreadLocal = new ThreadLocal<Enclave>() {
        @Override
        protected Enclave initialValue() {
            return EnclaveHolder.INSTANCE.getEnclave();
        }

    };

    @OnOpen
    public void onOpen(final Session session) {
        LOGGER.info("Opening session {}", session.getId());
    }

    @OnMessage
    public void onRequest(Session session, EnclaveRequest request) {

        LOGGER.info("Request {}", request.getClass());

        EnclaveRequestType type = request.getType();

        final WebSocketTemplate webSocketTemplate = new WebSocketTemplate(session);

        if (type == null) {
            throw new UnsupportedOperationException("Unsupported operation");
        }
        
        Enclave enclave = enclaveThreadLocal.get();
        switch (type) {
            case STATUS:
                Status status = enclave.status();
                webSocketTemplate.execute(s -> s.getBasicRemote().sendObject(new EnclaveResponse(type, status)));
                break;

            case DEFAULT_PUBLIC_KEY:
                PublicKey publicKey = enclave.defaultPublicKey();
                webSocketTemplate.execute(session1 -> session1.getBasicRemote().sendObject(new EnclaveResponse(type, publicKey)));
                break;

            case FORWARDING_KEYS:
                Set<PublicKey> forwardingKeys = enclave.getForwardingKeys();

                webSocketTemplate.execute(s -> s.getBasicRemote().sendObject(
                        new EnclaveResponse(type, forwardingKeys.toArray(new PublicKey[0]))));
                break;

            case PUBLIC_KEYS:
                Set<PublicKey> publicKeys = enclave.getPublicKeys();
                webSocketTemplate.execute(s -> s.getBasicRemote().sendObject(
                        new EnclaveResponse(type, publicKeys.toArray(new PublicKey[0]))));
                break;

            case ENCRYPT_PAYLOAD:

                byte[] message = (byte[]) request.getArgs().get(0);
                PublicKey senderPublicKey = (PublicKey) request.getArgs().get(1);
                List<PublicKey> recipientPublicKeys = (List<PublicKey>) request.getArgs().get(2);

                EncodedPayload payload = enclave.encryptPayload(message, senderPublicKey, recipientPublicKeys);
                webSocketTemplate.execute((s) -> s.getBasicRemote().sendObject(new EnclaveResponse(type, payload)));

                break;

            case ENCRYPT_RAWTXN_PAYLOAD:
                RawTransaction txn = RawTransaction.class.cast(request.getArgs().get(0));
                List<PublicKey> recipients = (List<PublicKey>) request.getArgs().get(1);

                EncodedPayload encRawPayload = enclave.encryptPayload(txn, recipients);
                webSocketTemplate.execute(s -> s.getBasicRemote().sendObject(new EnclaveResponse(type, encRawPayload)));
                break;

            case ENCRYPT_RAW_PAYLOAD:
                byte[] rawMessage = (byte[]) request.getArgs().get(0);

                PublicKey from = (PublicKey) request.getArgs().get(1);

                RawTransaction rawTransaction = enclave.encryptRawPayload(rawMessage, from);

                webSocketTemplate.execute(s -> s.getBasicRemote().sendObject(new EnclaveResponse(type, rawTransaction)));
                break;

            case UNENCRYPT_TXN:
                EncodedPayload unencryptPayload = (EncodedPayload) request.getArgs().get(0);
                PublicKey providedKey = (PublicKey) request.getArgs().get(1);
                byte[] txnData = enclave.unencryptTransaction(unencryptPayload, providedKey);
                webSocketTemplate.execute(s -> s.getBasicRemote().sendObject(new EnclaveResponse(type, ByteBuffer.wrap(txnData))));
                break;

            case CREATE_NEW_RECIPIENT_BOX:
                EncodedPayload createNewRecipientPayload = (EncodedPayload) request.getArgs().get(0);
                PublicKey recipientKey = (PublicKey) request.getArgs().get(1);

                byte[] boxData = enclave.createNewRecipientBox(createNewRecipientPayload, recipientKey);
                webSocketTemplate.execute(s -> s.getBasicRemote().sendObject(new EnclaveResponse(type, ByteBuffer.wrap(boxData))));
                break;
        }

    }

    @OnClose
    public void onClose(final Session session) {
        LOGGER.info("Closing session {}", session.getId());
    }

}
