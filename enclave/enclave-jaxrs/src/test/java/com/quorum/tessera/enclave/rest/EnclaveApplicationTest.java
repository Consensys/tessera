package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;

import static com.quorum.tessera.enclave.rest.Fixtures.createSample;
import com.quorum.tessera.service.Service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class EnclaveApplicationTest {

    private Enclave enclave;

    private JerseyTest jersey;

    @Before
    public void setUp() throws Exception {

        enclave = mock(Enclave.class);
        when(enclave.status()).thenReturn(Service.Status.STARTED);
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
        verify(enclave).status();
    }

    @Test
    public void pingServerDown() throws Exception {
        
        when(enclave.status()).thenReturn(Service.Status.STOPPED);
        
        Response response = jersey.target("ping").request().get();
        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.readEntity(String.class)).isEqualTo(Service.Status.STOPPED.name());
        verify(enclave).status();
    } 
    

    @Test
    public void encryptPayload() {

        EncodedPayload pay = createSample();

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
