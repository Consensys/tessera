
package com.quorum.tessera.server.websockets;


import java.io.IOException;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(
        value = "/status")
public class StatusEndpoint {
    
    @OnMessage
    public void echo(Session session,String data) throws IOException {
        session.getBasicRemote().sendText(data);
     }
    
}
