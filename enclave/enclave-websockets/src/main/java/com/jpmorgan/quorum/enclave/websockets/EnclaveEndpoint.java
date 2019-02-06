package com.jpmorgan.quorum.enclave.websockets;

import static com.jpmorgan.quorum.enclave.websockets.EnclaveRequestType.PUBLIC_KEYS;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.encryption.PublicKey;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void onOpen(Session session) {
        LOGGER.info("HELLO");
    }

    @OnMessage
    public void onRequest(Session session, EnclaveRequest request) {

        LOGGER.info("Request {}", request.getClass());

        EnclaveRequestType type = request.getType();

        final WebSocketTemplate webSocketTemplate = new WebSocketTemplate(session);

        if (type == EnclaveRequestType.DEFAULT_PUBLIC_KEY) {
            PublicKey publicKey = enclave.defaultPublicKey();
            webSocketTemplate.execute((WebSocketCallback) (Session session1) -> {
                session1.getBasicRemote().sendObject(publicKey);
            });
            return;
        }

        if (type == EnclaveRequestType.FORWARDING_KEYS) {
            Set<PublicKey> keys = enclave.getForwardingKeys();
            webSocketTemplate.execute((WebSocketCallback) (s) -> {
                s.getBasicRemote().sendObject(keys);
            });
            return;
        }

        if (type == PUBLIC_KEYS) {
            Set<PublicKey> keys = enclave.getPublicKeys();
            webSocketTemplate.execute((WebSocketCallback) (s) -> {
                s.getBasicRemote().sendObject(keys);
            });
            return;
        }

        if (type == EnclaveRequestType.ENCRYPT_PAYLOAD) {

            byte[] message = (byte[]) request.getArgs().get(0);
            PublicKey senderPublicKey = (PublicKey) request.getArgs().get(1);
            List<PublicKey> recipientPublicKeys = (List<PublicKey>) request.getArgs().get(2);

            EncodedPayload payload = enclave.encryptPayload(message, senderPublicKey, recipientPublicKeys);
            webSocketTemplate.execute((WebSocketCallback) (s) -> {
                s.getBasicRemote().sendObject(payload);
            });
            return;
        }

        if (type == EnclaveRequestType.ENCRYPT_RAWTXN_PAYLOAD) {

            RawTransaction txn = RawTransaction.class.cast(request.getArgs().get(0));
            List<PublicKey> recipientPublicKeys = (List<PublicKey>) request.getArgs().get(1);

            EncodedPayload payload = enclave.encryptPayload(txn, recipientPublicKeys);
            webSocketTemplate.execute((WebSocketCallback) (s) -> {
                s.getBasicRemote().sendObject(payload);
            });
            return;
        }
        
        if(type == EnclaveRequestType.ENCRYPT_RAW_PAYLOAD) {
            
            byte[] message = (byte[]) request.getArgs().get(0);
            
            PublicKey from = (PublicKey) request.getArgs().get(1);
            
            RawTransaction txn = enclave.encryptRawPayload(message, from);
            
            webSocketTemplate.execute((s) -> {
                s.getBasicRemote().sendObject(txn);
            });
            return;
           
        }
        
        if(type == EnclaveRequestType.UNENCRYPT_TXN) {
          
            EncodedPayload payload = (EncodedPayload) request.getArgs().get(0);
            PublicKey providedKey = (PublicKey) request.getArgs().get(1);
            byte[] txnData = enclave.unencryptTransaction(payload, providedKey);
            webSocketTemplate.execute((s) -> {
                s.getBasicRemote().sendBinary(ByteBuffer.wrap(txnData));
            });
            return;
        }
        
        if(type == EnclaveRequestType.CREATE_NEW_RECIPIENT_BOX) {
        
            EncodedPayload payload = (EncodedPayload) request.getArgs().get(0);
            PublicKey recipientKey = (PublicKey) request.getArgs().get(1);
            
            byte[] boxData = enclave.createNewRecipientBox(payload, recipientKey);
            webSocketTemplate.execute((s) -> {
                s.getBasicRemote().sendBinary(ByteBuffer.wrap(boxData));
            });
            return;
        }
        throw new UnsupportedOperationException(type + " is not supported");
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.info("BYE");
    }

}
