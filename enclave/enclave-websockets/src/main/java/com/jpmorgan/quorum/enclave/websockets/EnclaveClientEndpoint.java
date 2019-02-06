package com.jpmorgan.quorum.enclave.websockets;

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
        decoders = {EnclaveRequestCodec.class,PublicKeyCodec.class,PublicKeySetCodec.class,EncodedPayloadCodec.class},
        encoders = {EnclaveRequestCodec.class,PublicKeyCodec.class,PublicKeySetCodec.class,EncodedPayloadCodec.class}
)
public class EnclaveClientEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveClientEndpoint.class);

    private SynchronousQueue result = new SynchronousQueue<>();

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("HELLO");
    }

    @OnMessage
    public <T> void onResult(Session session, T response) {
        LOGGER.info("Response : {}",response);
        try{
            result.put(response);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }


    public <T> Optional<T> pollForResult(Class<T> type) {
        try{
            Object o = result.poll(2, TimeUnit.SECONDS);
            return Optional.ofNullable(type.cast(o));
        } catch (InterruptedException ex) {
            return Optional.empty();
        }
        
    }
    


    @OnClose
    public void onClose(Session session) {
        LOGGER.info("CLOSE");
    }
}
