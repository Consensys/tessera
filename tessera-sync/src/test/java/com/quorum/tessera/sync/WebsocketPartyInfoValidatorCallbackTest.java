
package com.quorum.tessera.sync;

import com.jpmorgan.quorum.mock.websocket.MockContainerProvider;
import com.quorum.tessera.partyinfo.model.Recipient;
import java.net.URI;
import java.util.concurrent.Executors;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


public class WebsocketPartyInfoValidatorCallbackTest {
    
    private WebsocketPartyInfoValidatorCallback websocketPartyInfoValidatorCallback;
    
   
    @Before
    public void onSetUp() {
        websocketPartyInfoValidatorCallback = new WebsocketPartyInfoValidatorCallback();
    }
    
    @Test
    public void requestDecode() throws Exception {
        WebSocketContainer mockWebSocketContainer = MockContainerProvider.getInstance();
        
        Session session = mock(Session.class);
        Async async = mock(Async.class);
        when(session.getAsyncRemote()).thenReturn(async);
        
        when(mockWebSocketContainer.connectToServer(any(Object.class), any(URI.class))).thenReturn(session);
        
        Recipient recipient
                 = mock(Recipient.class);
        when(recipient.getUrl()).thenReturn("http://some.com");
        
        byte[] encodedPayloadData = "Hellow".getBytes();
        
        Executors.newSingleThreadExecutor().submit(() -> {
            websocketPartyInfoValidatorCallback.onMessage("Outcome");
            return null;
        });
        
        String result = websocketPartyInfoValidatorCallback.requestDecode(recipient, encodedPayloadData);
        
        assertThat(result).isEqualTo("Outcome");
        reset(mockWebSocketContainer);
    }
    
}
