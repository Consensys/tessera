package com.quorum.tessera.enclave.websockets;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.websocket.CloseReason;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;

public class WebsocketEnclaveClientTest {
    
    @Test(expected = EnclaveCommunicationException.class)
    public void containerThrowsDeploymentException() throws Exception {
        
        URI serverUri = URI.create("ws:/localhost:9989");
        WebSocketContainer container = mock(WebSocketContainer.class);
        when(container.connectToServer(any(EnclaveClientEndpoint.class), any(URI.class)))
                .thenThrow(javax.websocket.DeploymentException.class);
        
        WebsocketEnclaveClient enclaveAdapter = new WebsocketEnclaveClient(container, serverUri);
        enclaveAdapter.start();
    }
    
    @Test(expected = EnclaveCommunicationException.class)
    public void containerThrowsIOException() throws Exception {
        
        URI serverUri = URI.create("ws:/localhost:9989");
        WebSocketContainer container = mock(WebSocketContainer.class);
        when(container.connectToServer(any(EnclaveClientEndpoint.class), any(URI.class)))
                .thenThrow(IOException.class);
        
        WebsocketEnclaveClient enclaveAdapter = new WebsocketEnclaveClient(container, serverUri);
        enclaveAdapter.start();
    }
    
    @Test
    public void onDestroyClosesRemoteSession() throws Exception {
        
        Session remoteSession = mock(Session.class);
        URI serverUri = URI.create("ws:/localhost:9989");
        
        WebSocketContainer container = mock(WebSocketContainer.class);
        when(container.connectToServer(any(EnclaveClientEndpoint.class), any(URI.class)))
                .thenReturn(remoteSession);
        
        WebsocketEnclaveClient enclaveAdapter = new WebsocketEnclaveClient(container, serverUri);
        enclaveAdapter.start();
        
        verifyZeroInteractions(remoteSession);
        
        List<CloseReason> results = new ArrayList<>();
        doAnswer((InvocationOnMock iom) -> {
            CloseReason closeReason = iom.getArgument(0);
            results.add(closeReason);
            return closeReason;
        }).when(remoteSession).close(any(CloseReason.class));
        
        enclaveAdapter.stop();
        
        assertThat(results).hasSize(1);
        
        CloseReason result = results.get(0);
        assertThat(result.getCloseCode()).isEqualTo(CloseReason.CloseCodes.NORMAL_CLOSURE);
        assertThat(result.getReasonPhrase()).isEqualTo("Bye");
        
        verify(remoteSession).close(any(CloseReason.class));
        verifyNoMoreInteractions(remoteSession);
        
    }
    
    @Test
    public void sessionCloseThrowsIOExceptionOnClose() throws Exception {
        
        Session remoteSession = mock(Session.class);
        URI serverUri = URI.create("ws:/localhost:9989");
        
        WebSocketContainer container = mock(WebSocketContainer.class);
        when(container.connectToServer(any(EnclaveClientEndpoint.class), any(URI.class)))
                .thenReturn(remoteSession);
        
        WebsocketEnclaveClient enclaveAdapter = new WebsocketEnclaveClient(container, serverUri);
        enclaveAdapter.start();
        
        verifyZeroInteractions(remoteSession);
        
        doThrow(IOException.class).when(remoteSession).close(any(CloseReason.class));
        
        enclaveAdapter.stop();
        
        verify(remoteSession).close(any(CloseReason.class));
        verifyNoMoreInteractions(remoteSession);
    }
}
