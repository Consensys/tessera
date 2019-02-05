package com.jpmorgan.quorum.enclave.websockets;

import static com.jpmorgan.quorum.enclave.websockets.EnclaveRequestType.PUBLIC_KEYS;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import java.util.List;
import java.util.Set;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/enclave",
        encoders = {EnclaveRequestCodec.class,EncodedPayloadCodec.class},
        decoders={EnclaveRequestCodec.class,EncodedPayloadCodec.class}
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
        
        if(type == EnclaveRequestType.ENCRYPT_PAYLOAD) {
            
            byte[] message = (byte[]) request.getArgs().get(0);
            PublicKey senderPublicKey = (PublicKey) request.getArgs().get(1);
            List<PublicKey> recipientPublicKeys = (List<PublicKey>) request.getArgs().get(2);
            
            EncodedPayload payload = enclave.encryptPayload(message, senderPublicKey, recipientPublicKeys);
            webSocketTemplate.execute((WebSocketCallback) (s) -> {
                s.getBasicRemote().sendObject(payload);
            });
            return;
        }

        throw new UnsupportedOperationException(String.format("%s is not a supported request type ", type));

    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.info("BYE");
    }

    @OnError
    public void onError(Session session, Throwable t) throws Throwable {
        LOGGER.error(null, t);
    }

}
