package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
import static com.quorum.tessera.test.Fixtures.*;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * This tests that a node that hosts multiple sets of keys can send/receive
 * transactions for both keys
 */
@RunWith(Parameterized.class)
public class MultipleKeyNodeIT {

    private static final URI SEND_SERVER_URI = NODE1_URI;

    private static final URI SERVER_URI = NODE3_URI;

    private final Client client = ClientBuilder.newClient();

    private final String recipientPublicKey;

    private String txHash;
    
    public MultipleKeyNodeIT(String recipientPublicKey) {
        this.recipientPublicKey = recipientPublicKey;
    }
    
    
    @Before
    public void onSetUp() {
        //Given a transaction is sent to NODE1 
        final SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(PTY1_KEY);
        sendRequest.setTo(recipientPublicKey);
        sendRequest.setPayload(TXN_DATA);

        final Response response = this.client.target(SEND_SERVER_URI)
                .path("/send")
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);

        final SendResponse result = response.readEntity(SendResponse.class);

        assertThat(result.getKey()).isNotBlank();
        
        this.txHash = result.getKey();
        
    }
    
    @Test
    public void thenTransactionHasBeenPersistedOnOtherNode() throws UnsupportedEncodingException {

        //retrieve the transaction
        final Response retrieveResponse = this.client.target(SERVER_URI)
                .path("transaction")
                .path(URLEncoder.encode(txHash, "UTF-8"))
                .request().get();

        assertThat(retrieveResponse).isNotNull();
        assertThat(retrieveResponse.getStatus()).isEqualTo(200);

        final ReceiveResponse result = retrieveResponse.readEntity(ReceiveResponse.class);

        assertThat(result.getPayload()).isEqualTo(TXN_DATA);

    }

    @Parameterized.Parameters
    public static List<String> recipientKeys() {
        return Arrays.asList(PTY4_KEY,PTY3_KEY);
    }
    
    
    

}
