package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.KeyVaultType;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class KeyVaultConfigOptionsTest {

  @Test
  public void getters() {
    KeyVaultConfigOptions opts = new KeyVaultConfigOptions();
    opts.vaultType = KeyVaultType.AZURE;
    opts.vaultUrl = "url";
    opts.hashicorpApprolePath = "approle";
    opts.hashicorpSecretEnginePath = "engine";
    Path keystore = Paths.get("keystore");
    Path truststore = Paths.get("truststore");
    opts.hashicorpTlsKeystore = keystore;
    opts.hashicorpTlsTruststore = truststore;

    assertThat(opts.getVaultType()).isEqualTo(KeyVaultType.AZURE);
    assertThat(opts.getVaultUrl()).isEqualTo("url");
    assertThat(opts.getHashicorpApprolePath()).isEqualTo("approle");
    assertThat(opts.getHashicorpSecretEnginePath()).isEqualTo("engine");
    assertThat(opts.getHashicorpTlsKeystore()).isEqualTo(keystore);
    assertThat(opts.getHashicorpTlsTruststore()).isEqualTo(truststore);
  }
}
