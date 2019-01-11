package com.quorum.tessera.enclave.rest;

import static com.quorum.tessera.enclave.rest.Fixtures.createSample;
import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.EncodedPayloadWithRecipients;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.json.JsonArray;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EnclaveApplicationTest {

    private Enclave enclave;

    private JerseyTest jersey;

    @Before
    public void setUp() throws Exception {

        enclave = mock(Enclave.class);

        jersey = Util.create(enclave);

        jersey.setUp();

    }

    @After
    public void tearDown() throws Exception {
        Mockito.verifyNoMoreInteractions(enclave);
        jersey.tearDown();

    }

    @Test
    public void ping() throws Exception {

        Response response = jersey.target("ping").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isNotEmpty();

    }

    @Test
    public void defaultKey() throws Exception {

        PublicKey publicKey = PublicKey.from("defaultKey".getBytes());

        when(enclave.defaultPublicKey()).thenReturn(publicKey);

        Response response = jersey.target("default").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("defaultKey");
        verify(enclave).defaultPublicKey();

    }

    @Test
    public void publicKeys() throws Exception {

        Set<PublicKey> keys = Stream.of("publicKey", "publicKey2")
                .map(String::getBytes)
                .map(PublicKey::from)
                .collect(Collectors.toSet());

        when(enclave.getPublicKeys())
                .thenReturn(keys);

        Response response = jersey.target("public").request().get();
        assertThat(response.getStatus()).isEqualTo(200);

        JsonArray result = response.readEntity(JsonArray.class);

        assertThat(result).hasSize(2);

        verify(enclave).getPublicKeys();
    }

    @Test
    public void encryptPayload() {

        EncodedPayloadWithRecipients pay = createSample();

        when(enclave.encryptPayload(any(byte[].class), any(PublicKey.class), anyList()
        )).thenReturn(pay);

        EnclavePayload enclavePayload = new EnclavePayload();
        enclavePayload.setData("SOMEDATA".getBytes());
        enclavePayload.setSenderKey("SENDER_KEY".getBytes());
        enclavePayload.setRecipientPublicKeys(Arrays.asList("RecipientPublicKey".getBytes()));
        Response response = jersey.target("encrypt")
                .request(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .post(Entity.entity(enclavePayload, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(200);

        byte[] result = response.readEntity(byte[].class);
        assertThat(result).isNotNull().isNotEmpty();

        verify(enclave).encryptPayload(any(byte[].class), any(PublicKey.class), anyList());

    }

}
