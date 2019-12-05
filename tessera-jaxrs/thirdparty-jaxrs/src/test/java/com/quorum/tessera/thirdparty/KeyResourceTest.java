package com.quorum.tessera.thirdparty;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonReader;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class KeyResourceTest {

    private KeyResource keyResource;

    private ConfigService configService;

    @Before
    public void onSetUp() {
        configService = mock(ConfigService.class);
        keyResource = new KeyResource(configService);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(configService);
    }

    @Test
    public void testGetPublicKeys() {

        Base64.Decoder base64Decoder = Base64.getDecoder();

        final String keyJsonString = "{\"keys\": [{\"key\": \"QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=\"}]}";

        String key = "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=";

        Set<PublicKey> publicKeys = new HashSet<>();
        publicKeys.add(PublicKey.from(base64Decoder.decode(key)));

        when(configService.getPublicKeys()).thenReturn(publicKeys);

        Response response = keyResource.getPublicKeys();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final String output = response.getEntity().toString();
        final JsonReader expected = Json.createReader(new StringReader(keyJsonString));
        final JsonReader actual = Json.createReader(new StringReader(output));

        assertThat(expected.readObject()).isEqualTo(actual.readObject());

        verify(configService).getPublicKeys();
    }
}
