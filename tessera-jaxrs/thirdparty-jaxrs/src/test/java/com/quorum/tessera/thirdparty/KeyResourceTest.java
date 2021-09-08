package com.quorum.tessera.thirdparty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.encryption.PublicKey;
import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KeyResourceTest {

  private KeyResource keyResource;

  private RuntimeContext runtimeContext;

  @Before
  public void onSetUp() {
    runtimeContext = mock(RuntimeContext.class);

    keyResource = new KeyResource();
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(runtimeContext);
  }

  @Test
  public void testGetPublicKeys() {

    try (var mockedStaticRuntimeContext = mockStatic(RuntimeContext.class)) {

      mockedStaticRuntimeContext.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

      Base64.Decoder base64Decoder = Base64.getDecoder();

      final String keyJsonString =
          "{\"keys\": [{\"key\": \"QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=\"}]}";

      String key = "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=";

      Set<PublicKey> publicKeys = new HashSet<>();
      publicKeys.add(PublicKey.from(base64Decoder.decode(key)));

      when(runtimeContext.getPublicKeys()).thenReturn(publicKeys);

      Response response = keyResource.getPublicKeys();

      assertThat(response).isNotNull();
      assertThat(response.getStatus()).isEqualTo(200);

      final String output = response.getEntity().toString();
      final JsonReader expected = Json.createReader(new StringReader(keyJsonString));
      final JsonReader actual = Json.createReader(new StringReader(output));

      assertThat(expected.readObject()).isEqualTo(actual.readObject());

      verify(runtimeContext).getPublicKeys();
      mockedStaticRuntimeContext.verify(RuntimeContext::getInstance);
      mockedStaticRuntimeContext.verifyNoMoreInteractions();
    }
  }
}
