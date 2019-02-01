package com.jpmorgan.quorum.enclave.websockets;

import java.io.IOException;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.Session;


public class WebSocketTemplate {
    
    private final Session session;

    public WebSocketTemplate(Session session) {
        this.session = session;
    }
    
    public void execute(WebSocketCallback callback) {
        try{
            callback.execute(session);
        } catch (IOException | DeploymentException | EncodeException ex) {
           throw new WebSocketException(ex);
        }
    }
    
}
