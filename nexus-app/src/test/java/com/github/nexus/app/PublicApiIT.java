
package com.github.nexus.app;

import com.github.api.nexus.quorum.v1.SomeObject;
import static com.github.nexus.app.Launcher.SERVER_URI;
import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;
import java.net.URI;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PublicApiIT {
    
    private static final URI SERVICE_URI = Launcher.SERVER_URI;
    
    private RestServer restServer;
     
    public PublicApiIT() {
    }
    
    @Before
    public void setUp() throws Exception {
        Nexus nexus = new Nexus();
        restServer = RestServerFactory.create().createServer(SERVER_URI, nexus);
        restServer.start();
    }
    
    @After
    public void tearDown() throws Exception {
        restServer.stop();
    }
    
    @Test
    public void requestVersion() {

        Client client = ClientBuilder.newClient();

        javax.ws.rs.core.Response response = client
                .target(SERVICE_URI)
                .path("/api/version")
                .request()
                .get();

        assertThat(response).isNotNull();
        assertThat(response.readEntity(String.class))
                .isEqualTo("No version defined yet!");
        assertThat(response.getStatus()).isEqualTo(200);

    }
    
    @Test
    public void createSomeObjectJson() {

        Client client = ClientBuilder.newClient();

        JsonObject someObject = Json.createObjectBuilder()
                .add("name","Lesley Crowther").build();

        javax.ws.rs.core.Response reponse = client
                .target(SERVICE_URI)
                .path("/some/thing")
                .request(MediaType.APPLICATION_JSON)
                .buildPost(Entity.entity(someObject.toString(), MediaType.APPLICATION_JSON))
                .invoke();

        assertThat(reponse).isNotNull();
        assertThat(reponse.getStatus()).isEqualTo(201);

    }
        @Test
    public void createSomeObjectXml() {

        Client client = ClientBuilder.newClient();


        javax.ws.rs.core.Response reponse = client
                .target(SERVICE_URI)
                .path("/some/thing")
                .request(MediaType.APPLICATION_XML)
                .buildPost(Entity.entity("<someObject name='Lesley Crowther' />", MediaType.APPLICATION_XML))
                .invoke();

        assertThat(reponse).isNotNull();
        assertThat(reponse.getStatus()).isEqualTo(201);

    }
}
