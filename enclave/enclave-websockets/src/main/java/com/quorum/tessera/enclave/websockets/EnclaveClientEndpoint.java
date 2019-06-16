package com.quorum.tessera.enclave.websockets;

import java.util.Optional;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import javax.websocket.ClientEndpoint;
import javax.websocket.Session;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint(
        encoders = {EnclaveRequestCodec.class},
        decoders = {
            EnclaveResponseCodec.class
        }
)
public class EnclaveClientEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveClientEndpoint.class);

    private SynchronousQueue result = new SynchronousQueue<>();

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.debug("HELLO {}", session.getId());
    }

    @OnMessage
    public <T> void onResult(Session session, EnclaveResponse<T> response) {
        LOGGER.debug("Response : {}", response);

        InterruptableCallback.execute(() -> {
            result.put(response.getPayload());
            return true;
        });
    }

    public <T> Optional<T> pollForResult(Class<T> type) {
        Object o = InterruptableCallback.execute(() -> result.poll(5, TimeUnit.SECONDS));
        return Optional.ofNullable(type.cast(o));
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.debug("Closing session {}", session.getId());
    }

}
