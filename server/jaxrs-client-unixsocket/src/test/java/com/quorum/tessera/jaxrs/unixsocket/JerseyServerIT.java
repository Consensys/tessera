package com.quorum.tessera.jaxrs.unixsocket;

import com.quorum.tessera.config.CommunicationType;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.UnixServerSocket;
import com.quorum.tessera.server.JerseyServer;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JerseyServerIT {

    private Path unixfile = Paths.get("/tmp/bogus.sock");

    private JerseyServer server;

    @Before
    public void onSetUp() throws Exception {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setCommunicationType(CommunicationType.REST);

        //InetServerSocket serverSocket = new InetServerSocket("http://localhost", 8080);
        UnixServerSocket serverSocket = new UnixServerSocket(unixfile.toString());

        serverConfig.setServerSocket(serverSocket);
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

        ClientConfig config = new ClientConfig();
        config.connectorProvider(new JerseyUnixSocketConnectorProvider());

        Response result = ClientBuilder.newClient(config)
                .property("unixfile", unixfile)
                .target(URI.create("http://localhost:88"))
                .path("ping")
                .request()
                .get();

        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.readEntity(String.class)).isEqualTo("HEllow");
    }

    @Test
    public void create() {

        ClientConfig config = new ClientConfig();
        config.connectorProvider(new JerseyUnixSocketConnectorProvider());

        SamplePayload payload = new SamplePayload();
        payload.setValue("Hellow");

        Response result = ClientBuilder.newClient(config)
                .property("unixfile", unixfile)
                .target(URI.create("http://localhost:88"))
                .path("create")
                .request()
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

        assertThat(result.getStatus()).isEqualTo(201);
        assertThat(result.getLocation()).isNotNull();

        Response result2 = ClientBuilder.newClient(config)
                .property("unixfile", unixfile)
                .target(result.getLocation())
                .request(MediaType.APPLICATION_JSON)
                .get();

        SamplePayload p = result2.readEntity(SamplePayload.class);
        assertThat(p).isNotNull();
        assertThat(p.getValue()).isEqualTo("Hellow");

        Response result3 = ClientBuilder.newClient(config)
                .property("unixfile", unixfile)
                .target(URI.create("http://localhost:88"))
                .path(p.getId()).request().delete();

        assertThat(result3.getStatus()).isEqualTo(200);
        SamplePayload deleted = result3.readEntity(SamplePayload.class);
        assertThat(deleted.getValue()).isEqualTo("Hellow");
    }

    @Test
    public void raw() throws Exception {

        ClientConfig config = new ClientConfig();
        config.connectorProvider(new JerseyUnixSocketConnectorProvider());
        Response result = ClientBuilder.newClient(config)
                .property("unixfile", unixfile)
                .target(URI.create("http://localhost:88"))
                .path("sendraw")
                .request()
                .header("c11n-from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
                .header("c11n-to", "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=")
                .post(Entity.entity("PAYLOAD".getBytes(), MediaType.APPLICATION_OCTET_STREAM_TYPE));

        assertThat(result.getStatus()).isEqualTo(201);
        

    }
}
