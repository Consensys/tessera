package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.enclave.websockets.WebSocketException;
import com.quorum.tessera.enclave.websockets.WebSocketCallback;
import com.quorum.tessera.enclave.websockets.WebSocketTemplate;
import java.io.IOException;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class WebSocketTemplateTest {

    @Test
    public void invokeSomeFunctionOnSessionObject() throws IOException {

        Session session = mock(Session.class);
        WebSocketTemplate template = new WebSocketTemplate(session);

        template.execute(s -> {
            s.close();
        });

        verify(session).close();

    }

    @Test
    public void sessionThrowsIOException() throws Exception {

        Session session = mock(Session.class);
        WebSocketCallback callback = mock(WebSocketCallback.class);

        WebSocketTemplate template = new WebSocketTemplate(session);
        doThrow(IOException.class).when(callback).execute(session);
        
        try{
            template.execute(callback);
            Assertions.failBecauseExceptionWasNotThrown(WebSocketException.class);
        } catch (WebSocketException ex) {
            assertThat(ex).hasCauseExactlyInstanceOf(IOException.class);
        }

    }

    @Test
    public void sessionThrowsEncodeException() throws Exception {

        Session session = mock(Session.class);
        WebSocketCallback callback = mock(WebSocketCallback.class);

        WebSocketTemplate template = new WebSocketTemplate(session);
        doThrow(EncodeException.class).when(callback).execute(session);
        try{
            template.execute(callback);
            Assertions.failBecauseExceptionWasNotThrown(WebSocketException.class);
        } catch (WebSocketException ex) {
            assertThat(ex).hasCauseExactlyInstanceOf(EncodeException.class);
        }
    }
    
    @Test
    public void sessionThrowsDeploymentException() throws Exception {

        Session session = mock(Session.class);
        WebSocketCallback callback = mock(WebSocketCallback.class);

        WebSocketTemplate template = new WebSocketTemplate(session);
        doThrow(DeploymentException.class).when(callback).execute(session);
        try{
            template.execute(callback);
            Assertions.failBecauseExceptionWasNotThrown(WebSocketException.class);
        } catch (WebSocketException ex) {
            assertThat(ex).hasCauseExactlyInstanceOf(DeploymentException.class);
        }
    }

}
