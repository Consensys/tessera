package com.quorum.tessera.test.rest;

import com.quorum.tessera.config.Peer;
import java.net.URI;
import java.util.UUID;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class AdminConfigIT {

    private static final URI SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    private static final Client client = ClientBuilder.newClient();

    
    @Test
    public void addPeer() {
        
        String url = "http://"+ UUID.randomUUID().toString().replaceAll("-", "");
        
        Peer peer = new Peer(url);
        
        Response response = client.target(SERVER_URI)
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
