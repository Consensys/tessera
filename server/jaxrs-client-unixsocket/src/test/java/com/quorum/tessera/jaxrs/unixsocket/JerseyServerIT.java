// package com.quorum.tessera.jaxrs.unixsocket;
//
// import com.quorum.tessera.config.CommunicationType;
// import com.quorum.tessera.config.ServerConfig;
// import com.quorum.tessera.server.jersey.JerseyServer;
// import org.glassfish.jersey.client.ClientConfig;
// import org.junit.After;
// import org.junit.Before;
// import org.junit.Test;
//
// import javax.ws.rs.client.Client;
// import javax.ws.rs.client.ClientBuilder;
// import javax.ws.rs.client.Entity;
// import javax.ws.rs.core.MediaType;
// import javax.ws.rs.core.Response;
// import java.net.URI;
//
// import static org.assertj.core.api.Assertions.assertThat;
//
// public class JerseyServerIT {
//
//    private URI unixfile = URI.create("unix:/tmp/bogus.sock");
//
//    private JerseyServer server;
//
//    @Before
//    public void onSetUp() throws Exception {
//
//        ServerConfig serverConfig = new ServerConfig();
//        serverConfig.setCommunicationType(CommunicationType.REST);
//
//        serverConfig.setServerAddress(unixfile.toString());
//
//        server = new JerseyServer(serverConfig, SampleApplication.class);
//
//        server.start();
//    }
//
//    @After
//    public void onTearDown() {
//        server.stop();
//    }
//
//    @Test
//    public void ping() {
//
//        Response result =
// newClient(unixfile).target(URI.create("http://localhost:88")).path("ping").request().get();
//
//        assertThat(result.getStatus()).isEqualTo(200);
//        assertThat(result.readEntity(String.class)).isEqualTo("HEllow");
//    }
//
//    @Test
//    public void create() {
//
//        SamplePayload payload = new SamplePayload();
//        payload.setValue("Hellow");
//
//        Response result =
//            newClient(unixfile)
//                .target(unixfile)
//                .path("create")
//                .request()
//                .post(Entity.entity(payload, MediaType.APPLICATION_JSON));
//
//        assertThat(result.getStatus()).isEqualTo(201);
//        assertThat(result.getLocation()).isNotNull();
//
//        Response result2 =
// newClient(unixfile).target(result.getLocation()).request(MediaType.APPLICATION_JSON).get();
//
//        SamplePayload p = result2.readEntity(SamplePayload.class);
//        assertThat(p).isNotNull();
//        assertThat(p.getValue()).isEqualTo("Hellow");
//
//        Response result3 =
// newClient(unixfile).target(unixfile).path(p.getId()).request().delete();
//
//        assertThat(result3.getStatus()).isEqualTo(200);
//        SamplePayload deleted = result3.readEntity(SamplePayload.class);
//        assertThat(deleted.getValue()).isEqualTo("Hellow");
//    }
//
//    @Test
//    public void raw() {
//
//        ClientConfig config = new ClientConfig();
//        config.connectorProvider(new JerseyUnixSocketConnectorProvider());
//        Response result =
//            newClient(unixfile)
//                .target(unixfile)
//                .path("sendraw")
//                .request()
//                .header("c11n-from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
//                .header("c11n-to", "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=")
//                .post(Entity.entity("PAYLOAD".getBytes(),
// MediaType.APPLICATION_OCTET_STREAM_TYPE));
//
//        assertThat(result.getStatus()).isEqualTo(201);
//    }
//
//    @Test
//    public void param() {
//        // URL.setURLStreamHandlerFactory(new UnixSocketURLStreamHandlerFactory());
//        ClientConfig config = new ClientConfig();
//        config.connectorProvider(new JerseyUnixSocketConnectorProvider());
//
//        Response result =
//            newClient(unixfile)
//                .target(unixfile)
//                .path("param")
//                .queryParam("queryParam", "QueryParamValue")
//                .request()
//                .header("headerParam", "HeaderParamValue")
//                .get();
//
//        assertThat(result.getStatus()).isEqualTo(200);
//    }
//
//    private static Client newClient(URI unixfile) {
//        ClientConfig config = new ClientConfig();
//        config.connectorProvider(new JerseyUnixSocketConnectorProvider());
//
//        return ClientBuilder.newClient(config).property("unixfile", unixfile);
//    }
// }
