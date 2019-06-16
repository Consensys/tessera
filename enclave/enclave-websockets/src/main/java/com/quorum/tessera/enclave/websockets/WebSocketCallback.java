package com.quorum.tessera.enclave.websockets;

import java.io.IOException;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.Session;


public interface WebSocketCallback<T> {
    void execute(Session session) throws IOException,DeploymentException,EncodeException;
}
