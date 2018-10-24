package com.quorum.tessera.test.rest;

import com.quorum.tessera.config.Peer;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyFactory;
import com.quorum.tessera.test.RestPartyFactory;
import java.net.URI;
import java.util.UUID;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class AdminConfigIT {

    private final Client client = ClientBuilder.newClient();

    private final PartyFactory partyFactory = new RestPartyFactory();
    
    @Test
    public void addPeer() {
        
        Party party = partyFactory.getParties().findAny().get();
        
        String url = "http://"+ UUID.randomUUID().toString().replaceAll("-", "");
        
        Peer peer = new Peer(url);
        
        Response response = client.target(party.getUri())
                .path("config")
                .path("peers")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).put(Entity.entity(peer, MediaType.APPLICATION_JSON));
        
        
        assertThat(response.getStatus()).isEqualTo(201);
        
       URI location =  response.getLocation();
        
        Response queryResponse = client.target(location)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).get();
        
         assertThat(queryResponse.getStatus()).isEqualTo(200);
        
         assertThat(queryResponse.readEntity(Peer.class)).isEqualTo(peer);
        
        
    }
    
    
}
