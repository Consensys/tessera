package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
import static com.quorum.tessera.test.Fixtures.*;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * retrieve tx with hash retrieve tx with hash and sender retrieve hash that
 * doesn't exist
 */
public class ReceiveIT {
    
    private static final String RECEIVE_PATH = "transaction";
    
    private static final Client client = ClientBuilder.newClient();
    
    private String encodedHash;
    
    private String encodedSender;
    
    private String encodedRecipientOne;

    //Persist a single transaction that can be used later
    @Before
    public void init() throws UnsupportedEncodingException {
        
        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(SENDER_KEY);
        sendRequest.setTo(RECIPIENT_ONE);
        sendRequest.setPayload(TXN_DATA);
        
        final Response response = client.target(NODE1_URI)
                .path("/send")
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus()).isEqualTo(201);
        
        final SendResponse result = response.readEntity(SendResponse.class);
        
        final String hash = result.getKey();
        
        this.encodedHash = URLEncoder.encode(hash, UTF_8.toString());
        this.encodedSender = URLEncoder.encode(SENDER_KEY, UTF_8.toString());
        this.encodedRecipientOne = URLEncoder.encode(RECIPIENT_ONE, UTF_8.toString());
        
    }
    
    @Test
    public void fetchExistingTransactionUsingOwnKey() {
        
        final Response response = client.target(NODE1_URI)
                .path(RECEIVE_PATH + "/" + this.encodedHash)
                .request()
                .buildGet()
                .property("to", this.encodedSender)
                .invoke();

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        
        final ReceiveResponse result = response.readEntity(ReceiveResponse.class);
        
        assertThat(result.getPayload())
                .isEqualTo(TXN_DATA);
        
    }
    
    @Test
    public void fetchExistingTransactionUsingRecipientKey() throws Exception {
        
        final Response response = client.target(NODE1_URI)
                .path(RECEIVE_PATH)
                .path(encodedHash)
                .request()
                .property("to", this.encodedRecipientOne)
                .get();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        
        final ReceiveResponse result = response.readEntity(ReceiveResponse.class);
        
        assertThat(result.getPayload())
                .isEqualTo(TXN_DATA);
        
    }
    
    @Test
    public void fetchExistingTransactionNotUsingKey() throws UnsupportedEncodingException {
        
        final Response response = client.target(NODE1_URI)
                .path(RECEIVE_PATH)
                .path(encodedHash)
                .request()
                .buildGet()
                .invoke();

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        
        final ReceiveResponse result = response.readEntity(ReceiveResponse.class);
        
        assertThat(result.getPayload())
                .isEqualTo(TXN_DATA);
        
    }
    
    @Test
    public void fetchNonexistantTransactionFails() {
        
        final Response response = client.target(NODE1_URI)
                .path(RECEIVE_PATH)
                .path("invalidhashvalue")
                .request()
                .get();

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
        
        final String result = response.readEntity(String.class);
        assertThat(result).isEqualTo("Message with hash invalidhashvalue was not found");
        
    }
    
}
