package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import javax.websocket.ClientEndpoint;
import javax.websocket.Session;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint(encoders = {EnclaveRequestCodec.class}, decoders = {EnclaveResponseCodec.class})
public class EnclaveClientEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveClientEndpoint.class);

    private SynchronousQueue result = new SynchronousQueue<>();

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("HELLO");
    }

    @OnMessage
    public void onResult(Session session, EnclaveResponse response) {

        try{
            result.put(response.getResult());
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }



    public PublicKey getDefaultKey() {
        try{
            return (PublicKey) result.take();
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        } finally {
            result.clear();
        }
        return null;
    }

    public Set<PublicKey> getForwardingKeys() {
        try{
            return (Set<PublicKey>) result.take();
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
        return null;
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.info("CLOSE");
    }
}
