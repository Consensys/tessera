package com.quorum.tessera.sync;

import java.io.IOException;
import java.io.UncheckedIOException;
import javax.websocket.DecodeException;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;

@FunctionalInterface
public interface WebSocketSessionCallback<T> {

    T doExecute() throws EncodeException, DeploymentException, IOException, DecodeException;

    static <T> T execute(WebSocketSessionCallback<T> callback) {
        try {
            return callback.doExecute();
        } catch (EncodeException | DeploymentException | DecodeException ex) {
            throw new UncheckedWebSocketException(ex);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
