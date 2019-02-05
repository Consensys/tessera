package com.jpmorgan.quorum.enclave.websockets;

import java.io.IOException;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebSocketTemplate {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketTemplate.class);
    
    private final Session session;

    public WebSocketTemplate(Session session) {
        this.session = session;
    }
    
    public void execute(WebSocketCallback callback) {
        try{
            LOGGER.info("Before {}",session.getId());
            callback.execute(session);
            LOGGER.info("After {}",session.getId());
        } catch (IOException | DeploymentException | EncodeException ex) {
           throw new WebSocketException(ex);
        }
    }
    
}
