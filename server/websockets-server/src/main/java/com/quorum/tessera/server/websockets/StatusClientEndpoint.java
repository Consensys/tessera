
package com.quorum.tessera.server.websockets;

import java.io.IOException;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint
public class StatusClientEndpoint {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusClientEndpoint.class);
    
    @OnMessage
    public void handleEcho(String data) throws IOException {
        LOGGER.info("Echo {}",data);
     }
    
    @OnError
    public void onError(Session session, Throwable ex) {
        LOGGER.trace(null,ex);
    }
}
