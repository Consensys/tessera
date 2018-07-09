//package com.github.tessera;
//
//import com.github.tessera.node.PartyInfoParser;
//import com.github.tessera.node.model.PartyInfo;
//import com.github.tessera.api.Tessera;
//import com.github.tessera.service.locator.ServiceLocator;
//import org.glassfish.jersey.test.JerseyTest;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import javax.json.Json;
//import javax.ws.rs.client.Entity;
//import javax.ws.rs.core.Application;
//import javax.ws.rs.core.MediaType;
//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import java.util.Arrays;
//import java.util.Base64;
//import java.util.Collections;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//public class NexusIT extends JerseyTest {
//
//    @Override
//    protected Application configure() {
//        Launcher.cliArgumentList = Arrays.asList(
//            "-publicKeys", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=",
//            "-privateKeys", "{\"data\":{\"bytes\":\"yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=\"},\"type\":\"unlocked\"}"
//        );
//
//        final ServiceLocator serviceLocator = ServiceLocator.create();
//        return new Tessera(serviceLocator, "tessera-spring.xml");
//    }
//
//    @Test
//    public void sendSingleTransactionToSingleParty() {
//
//        String sendRequest = Json.createObjectBuilder()
//            .add("from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
//            .add("to", Json.createArrayBuilder().add("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc="))
//            .add("payload", "Zm9v").build().toString();
//
//        javax.ws.rs.core.Response response = target()
//            .path("/send")
//            .request()
//            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));
//
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStatus()).isEqualTo(201);
//
//    }
//
//    /**
//     * Quorum sends transaction with public key not in PartyInfo store.
//     */
//    @Ignore
//    @Test
//    public void sendSingleTransactionToMultipleParties() {
//        String sendRequest = Json.createObjectBuilder()
//            .add("from", "bXlwdWJsaWNrZXk=")
//            .add("to", Json.createArrayBuilder()
//                .add("cmVjaXBpZW50MQ==")
//                .add(Base64.getEncoder().encodeToString("HELLOW".getBytes()))
//            )
//            .add("payload", "Zm9v").build().toString();
//
//        javax.ws.rs.core.Response response = target()
//            .path("/send")
//            .request()
//            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));
//
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStatus()).isEqualTo(201);
//    }
//
//    /**
//     * Quorum sends transaction with public key not in PartyInfo store.
//     */
//    @Ignore
//    @Test
//    public void sendUnknownPublicKey() {
//        String sendRequest = Json.createObjectBuilder()
//            .add("from", "bXlwdWJsaWNrZXk=")
//            .add("to", Json.createArrayBuilder()
//                .add(Base64.getEncoder().encodeToString("BOGUS".getBytes()))
//            )
//            .add("payload", "Zm9v").build().toString();
//
//        javax.ws.rs.core.Response response = target()
//            .path("/transaction/send")
//            .request()
//            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));
//
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStatus()).isEqualTo(404);
//    }
//
//    @Ignore
//    @Test
//    public void partyInfo() {
//        PartyInfo partyInfo = new PartyInfo("http://someurl.com", Collections.emptySet(), Collections.emptySet());
//        byte[] encoded = PartyInfoParser.create().to(partyInfo);
//        InputStream data = new ByteArrayInputStream(encoded);
//
//        javax.ws.rs.core.Response response = target()
//            .path("/partyinfo")
//            .request()
//            .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStatus()).isEqualTo(201);
//    }
//
//    @Test
//    public void upcheck() {
//        javax.ws.rs.core.Response response = target()
//            .path("/upcheck")
//            .request()
//            .get();
//
//        assertThat(response).isNotNull();
//        assertThat(response.readEntity(String.class))
//            .isEqualTo("I'm up!");
//        assertThat(response.getStatus()).isEqualTo(200);
//    }
//
//    @Test
//    public void requestVersion() {
//
//        javax.ws.rs.core.Response response = target()
//            .path("/version")
//            .request()
//            .get();
//
//        assertThat(response).isNotNull();
//        assertThat(response.readEntity(String.class))
//            .isEqualTo("No version defined yet!");
//        assertThat(response.getStatus()).isEqualTo(200);
//
//    }
//
//}
