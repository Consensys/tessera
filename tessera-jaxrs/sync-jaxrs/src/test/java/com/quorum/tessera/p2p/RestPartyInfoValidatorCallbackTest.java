package com.quorum.tessera.p2p;

import com.quorum.tessera.jaxrs.mock.MockClient;
import com.quorum.tessera.partyinfo.model.Recipient;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RestPartyInfoValidatorCallbackTest {

    private RestPartyInfoValidatorCallback restPartyInfoValidatorCallback;

    private MockClient restClient;

    @Before
    public void onSetUp() {
        restClient = new MockClient();
        restPartyInfoValidatorCallback = new RestPartyInfoValidatorCallback(restClient);
    }
    
    @After
    public void onTearDown() {
        
    }

    @Test
    public void requestDecode() {

        String url = "http://somedomain.com/some";

        Response response = mock(Response.class);
        when(response.readEntity(String.class)).thenReturn("decodedData");
        
        when(restClient.getWebTarget().getMockInvocationBuilder().post(any(Entity.class)))
                .thenReturn(response);

        Recipient recipient = mock(Recipient.class);
        when(recipient.getUrl()).thenReturn(url);

        byte[] encodedPayloadData = "encodedPayloadData".getBytes();

        String result = restPartyInfoValidatorCallback.requestDecode(recipient, encodedPayloadData);

        assertThat(result).isEqualTo("decodedData");

    }

}
