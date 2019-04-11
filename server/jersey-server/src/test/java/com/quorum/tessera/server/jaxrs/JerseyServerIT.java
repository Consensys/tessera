package com.quorum.tessera.server.jaxrs;

import com.quorum.tessera.config.CommunicationType;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.JerseyServer;
import java.net.URI;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JerseyServerIT {

    private URI serverUri = URI.create("http://localhost:8080");

    private JerseyServer server;

    @Before
    public void onSetUp() throws Exception {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setServerAddress("http://localhost:8080");
        Application sample = new SampleApplication();
        server = new JerseyServer(serverConfig, sample);

        server.start();
    }

    @After
    public void onTearDown() throws Exception {
        server.stop();
    }

    @Test
    public void ping() {

        Response result = ClientBuilder.newClient()
                .target(serverUri)
                .path("ping")
                .request()
                .get();

        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.readEntity(String.class)).isEqualTo("HEllow");
    }

    @Test
    public void create() {

        SamplePayload payload = new SamplePayload();
        payload.setValue("Hellow");

        Response result = ClientBuilder.newClient()
                .target(serverUri)
                .path("create")
                .request()
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

        assertThat(result.getStatus()).isEqualTo(201);
        assertThat(result.getLocation()).isNotNull();

        Response result2 = ClientBuilder.newClient()
                .target(result.getLocation())
                .request(MediaType.APPLICATION_JSON)
                .get();

        SamplePayload p = result2.readEntity(SamplePayload.class);
        assertThat(p).isNotNull();
        assertThat(p.getValue()).isEqualTo("Hellow");

        Response result3 = ClientBuilder.newClient()
                .target(serverUri)
                .path(p.getId()).request().delete();

        assertThat(result3.getStatus()).isEqualTo(200);
        SamplePayload deleted = result3.readEntity(SamplePayload.class);
        assertThat(deleted.getValue()).isEqualTo("Hellow");
    }
}
