package com.quorum.tessera.test.rest;

import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
/**
 * Tests that recipients specified in the forwarding list receive a transaction
 * <p>
 * Node 3 should send all transactions to node 1
 */
public class TransactionForwardingIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionForwardingIT.class);
    
    private static final URI NODE_ONE = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    private static final URI NODE_TWO = UriBuilder.fromUri("http://127.0.0.1").port(8081).build();

    private static final URI NODE_THREE = UriBuilder.fromUri("http://127.0.0.1").port(8082).build();

    private final Client client = ClientBuilder.newClient();

    @Test
    public void sendTransactionToNode3AddsNode1AsRecipient() throws UnsupportedEncodingException {

        final String hash = this.sendNewTransaction(NODE_THREE, "giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=");

        //check the transaction is in node 1
        final Response response = this.client.target(NODE_ONE)
            .path("/transaction/" + URLEncoder.encode(hash, UTF_8.toString()))
            .request()
            .buildGet()
            .property("to", URLEncoder.encode("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", UTF_8.toString()))
            .invoke();


        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final String result = response.readEntity(String.class);
        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("payload");
        assertThat(jsonResult.getString("payload")).isEqualTo("Zm9v");

    }

    @Test
    public void sendTransactionToNode2DoesNotAddNode1AsRecipient() throws UnsupportedEncodingException {

        final String hash = this.sendNewTransaction(NODE_TWO, "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=");

        //check the transaction is not in node 1
        final Response response = this.client.target(NODE_ONE)
            .path("/transaction/" + URLEncoder.encode(hash, UTF_8.toString()))
            .request()
            .buildGet()
            .property("to", URLEncoder.encode("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", UTF_8.toString()))
            .invoke();

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);

    }

    @Test
    public void sendTransactionToNode3DoesNotAddNode2AsRecipient() throws UnsupportedEncodingException {

        final String hash = this.sendNewTransaction(NODE_THREE, "giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=");

        //check the transaction is in node 1
        final Response response = this.client.target(NODE_TWO)
            .path("/transaction/" + URLEncoder.encode(hash, UTF_8.toString()))
            .request()
            .buildGet()
            .property("to", URLEncoder.encode("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", UTF_8.toString()))
            .invoke();

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);

    }

    /**
     * Sends a new transaction to the given node
     *
     * @param node the target node for the new transaction
     * @return the hash of the transaction
     */
    private String sendNewTransaction(final URI node, final String from) {
        final String sendRequest = Json.createObjectBuilder()
            .add("from", from)
            .add("to", Json.createArrayBuilder())
            .add("payload", "Zm9v").build().toString();

        
        LOGGER.debug("Sending {} to {}", sendRequest,node);
        
        final Response response = this.client.target(node)
            .path("/send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        
        LOGGER.debug("Sent {} to {}", sendRequest,node);
        //check the call was success
        final String result = response.readEntity(String.class);
        LOGGER.debug("Response status : {}, body: {}", response.getStatus(),result);
        
         assertThat(response.getStatus()).isEqualTo(200);
        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("key");
        assertThat(response).isNotNull();
       

        return jsonResult.getString("key");
    }

}
