package com.jpmorgan.quorum.mock.websocket;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import static org.mockito.Mockito.*;

public class MockContainerProvider extends ContainerProvider {

    private static final WebSocketContainer INSTANCE = mock(WebSocketContainer.class);

    public static WebSocketContainer getInstance() {
        return INSTANCE;
    }

    @Override
    protected WebSocketContainer getContainer() {
        return INSTANCE;
    }
}
