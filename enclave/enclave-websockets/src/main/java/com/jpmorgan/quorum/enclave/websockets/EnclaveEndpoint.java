package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.encryption.PublicKey;
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
        decoders = {
            EnclaveRequestCodec.class, 
            PublicKeyCodec.class, 
            PublicKeySetCodec.class, 
            EncodedPayloadCodec.class, 
            RawTransactionCodec.class
        },
        encoders = {
            EnclaveRequestCodec.class, 
            PublicKeyCodec.class, 
            PublicKeySetCodec.class, 
            EncodedPayloadCodec.class, 
            RawTransactionCodec.class
        }
)
public class EnclaveEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveEndpoint.class);

    private Enclave enclave = EnclaveFactory.newFactory().create();

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

        switch (type) {
            case DEFAULT_PUBLIC_KEY:
                PublicKey publicKey = enclave.defaultPublicKey();
                webSocketTemplate.execute(session1 -> session1.getBasicRemote().sendObject(publicKey));
                break;

            case FORWARDING_KEYS:
                Set<PublicKey> forwardingKeys = enclave.getForwardingKeys();
                webSocketTemplate.execute(s -> s.getBasicRemote().sendObject(forwardingKeys));
                break;

            case PUBLIC_KEYS:
                Set<PublicKey> publicKeys = enclave.getPublicKeys();
                webSocketTemplate.execute(s -> s.getBasicRemote().sendObject(publicKeys));
                break;

            case ENCRYPT_PAYLOAD:
                byte[] message = (byte[]) request.getArgs().get(0);
                PublicKey senderPublicKey = (PublicKey) request.getArgs().get(1);
                List<PublicKey> recipientPublicKeys = (List<PublicKey>) request.getArgs().get(2);

                EncodedPayload payload = enclave.encryptPayload(message, senderPublicKey, recipientPublicKeys);
                webSocketTemplate.execute((s) -> s.getBasicRemote().sendObject(payload));
                break;

            case ENCRYPT_RAWTXN_PAYLOAD:
                RawTransaction txn = RawTransaction.class.cast(request.getArgs().get(0));
                List<PublicKey> recipients = (List<PublicKey>) request.getArgs().get(1);

                EncodedPayload encRawPayload = enclave.encryptPayload(txn, recipients);
                webSocketTemplate.execute(s -> s.getBasicRemote().sendObject(encRawPayload));
                break;

            case ENCRYPT_RAW_PAYLOAD:
                byte[] rawMessage = (byte[]) request.getArgs().get(0);

                PublicKey from = (PublicKey) request.getArgs().get(1);

                RawTransaction rawTransaction = enclave.encryptRawPayload(rawMessage, from);

                webSocketTemplate.execute(s -> s.getBasicRemote().sendObject(rawTransaction));
                break;

            case UNENCRYPT_TXN:
                EncodedPayload unencryptPayload = (EncodedPayload) request.getArgs().get(0);
                PublicKey providedKey = (PublicKey) request.getArgs().get(1);
                byte[] txnData = enclave.unencryptTransaction(unencryptPayload, providedKey);
                webSocketTemplate.execute(s -> s.getBasicRemote().sendBinary(ByteBuffer.wrap(txnData)));
                break;

            case CREATE_NEW_RECIPIENT_BOX:
                EncodedPayload createNewRecipientPayload = (EncodedPayload) request.getArgs().get(0);
                PublicKey recipientKey = (PublicKey) request.getArgs().get(1);

                byte[] boxData = enclave.createNewRecipientBox(createNewRecipientPayload, recipientKey);
                webSocketTemplate.execute(s -> s.getBasicRemote().sendBinary(ByteBuffer.wrap(boxData)));
                break;
        }

    }

    @OnClose
    public void onClose(final Session session) {
        LOGGER.info("Closing session {}", session.getId());
    }

}
