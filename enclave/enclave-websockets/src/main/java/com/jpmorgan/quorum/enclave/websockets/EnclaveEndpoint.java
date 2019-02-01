package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
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
        decoders = {EnclaveRequestCodec.class},
        encoders = PublicKeyCodec.class)
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

        if (type == EnclaveRequestType.DEFAULT_PUBLIC_KEY) {
            PublicKey publicKey = enclave.defaultPublicKey();

            WebSocketTemplate webSocketTemplate = new WebSocketTemplate(session);

            EnclaveResponse<PublicKey> response = EnclaveResponse.Builder.create()
                    .withType(EnclaveRequestType.DEFAULT_PUBLIC_KEY)
                    .withResult(publicKey)
                    .build();
            
            webSocketTemplate.execute((WebSocketCallback) (Session session1) -> {
                session1.getBasicRemote().sendObject(response);
            });
        }

        if (type == EnclaveRequestType.FORWARDING_KEYS) {
            Set<PublicKey> keys = enclave.getForwardingKeys();
            
            EnclaveResponse<Set<PublicKey>> response = EnclaveResponse.Builder.create()
                    .withType(EnclaveRequestType.FORWARDING_KEYS)
                    .withResult(keys)
                    .build();
            
            WebSocketTemplate webSocketTemplate = new WebSocketTemplate(session);

            webSocketTemplate.execute((WebSocketCallback) (Session session1) -> {
                session1.getBasicRemote().sendObject(keys);
            });
        }

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
