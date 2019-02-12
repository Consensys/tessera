package com.jpmorgan.quorum.enclave.websockets;

import java.util.concurrent.Executors;
import javax.websocket.Session;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class EnclaveClientEndpointTest {

    private EnclaveClientEndpoint enclaveClientEndpoint = new EnclaveClientEndpoint();

    @Test
    public void onOpen() {
        Session session = mock(Session.class);
        enclaveClientEndpoint.onOpen(session);
        verify(session).getId();
        verifyNoMoreInteractions(session);
    }

    @Test
    public void onClose() {
        Session session = mock(Session.class);
        enclaveClientEndpoint.onClose(session);
        verify(session).getId();
        verifyNoMoreInteractions(session);
    }

    @Test
    public void onResult() {

        Session session = mock(Session.class);

        Executors.newSingleThreadExecutor().submit(() -> {
            enclaveClientEndpoint.onResult(session, Boolean.TRUE);
        });
        
        Boolean result = enclaveClientEndpoint.pollForResult(Boolean.class).get();

        assertThat(result).isTrue();
    }

}
