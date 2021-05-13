package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.Config;
import java.util.Base64;
import org.junit.Test;

public class PayloadDigestTest {

  @Test
  public void defaultDigest() {
    PayloadDigest digest = new PayloadDigest.Default();
    String cipherText = "cipherText";
    byte[] result = digest.digest(cipherText.getBytes());

    assertThat(result).isNotNull();
    assertThat(result).hasSize(64);
  }

  @Test
  public void digest32Bytes() {
    PayloadDigest digest = new PayloadDigest.SHA512256();
    String cipherText = "cipherText";
    byte[] result = digest.digest(cipherText.getBytes());

    // This is what Orion would have generated
    final String expectedB64 = "7AagSZbaNRe/IJzrUKTp8Hl60wncQL1DHvDJCVQ+YIk=";

    assertThat(result).isNotNull();
    assertThat(result).hasSize(32);
    String resultInBase64 = Base64.getEncoder().encodeToString(result);
    assertThat(resultInBase64).isEqualTo(expectedB64);
  }

  @Test
  public void create() {
    Config config = mock(Config.class);
    assertThat(PayloadDigest.create(config)).isNotNull().isInstanceOf(PayloadDigest.Default.class);

    when(config.getClientMode()).thenReturn(ClientMode.ORION);
    assertThat(PayloadDigest.create(config))
        .isNotNull()
        .isInstanceOf(PayloadDigest.SHA512256.class);
  }
}
