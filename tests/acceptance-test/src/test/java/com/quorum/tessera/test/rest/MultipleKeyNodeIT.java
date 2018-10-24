package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.SendResponse;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyFactory;
import com.quorum.tessera.test.RestPartyFactory;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

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


    
    private static PartyFactory partyFactory = new RestPartyFactory();

    private final Client client = ClientBuilder.newClient();

    private final Party recipient;

    private String txHash;
    
    private RestUtils restUtils = new RestUtils();
    
    public MultipleKeyNodeIT(Party recipient) {
        this.recipient = recipient;
    }
    
    
    @Before
    public void onSetUp() {
        
        Party sender = partyFactory.findByAlias("A");

        byte[] transactionData = restUtils.createTransactionData();
        final SendResponse result = restUtils.sendRequestAssertSuccess(sender, transactionData, recipient);

        assertThat(result.getKey()).isNotBlank();
        
        this.txHash = result.getKey();
        
    }
    
    @Test
    public void thenTransactionHasBeenPersistedOnOtherNode() throws UnsupportedEncodingException {
        
        final byte[] transactionData = RestUtils.generateTransactionData();
        
        Party sender = partyFactory.findByAlias("A");
        
        //retrieve the transaction
        final Response retrieveResponse = this.client.target(sender.getPublicKey())
                .path("transaction")
                .path(URLEncoder.encode(txHash, "UTF-8"))
                .request().get();

        assertThat(retrieveResponse).isNotNull();
        assertThat(retrieveResponse.getStatus()).isEqualTo(200);

        final ReceiveResponse result = retrieveResponse.readEntity(ReceiveResponse.class);

        assertThat(result.getPayload()).isEqualTo(transactionData);

    }

    @Parameterized.Parameters
    public static List<Party> recipients() {
        return partyFactory.getParties()
            .filter(p -> p.getAlias().equals("D"))
            .filter(p -> p.getAlias().equals("C")).collect(Collectors.toList());
    }
    
    
    

}
