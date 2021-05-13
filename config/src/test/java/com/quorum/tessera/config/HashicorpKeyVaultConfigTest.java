package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;

public class HashicorpKeyVaultConfigTest {

  private HashicorpKeyVaultConfig vaultConfig;

  @Before
  public void setUp() {
    vaultConfig = new HashicorpKeyVaultConfig();
  }

  @Test
  public void multiArgConstructor() {
    String url = "url";
    String approle = "approle";
    Path keyStore = Paths.get("keystore");
    Path trustStore = Paths.get("truststore");

    HashicorpKeyVaultConfig conf = new HashicorpKeyVaultConfig(url, approle, keyStore, trustStore);

    assertThat(conf.getUrl()).isEqualTo(url);
    assertThat(conf.getApprolePath()).isEqualTo("approle");
    assertThat(conf.getTlsKeyStorePath()).isEqualTo(keyStore);
    assertThat(conf.getTlsTrustStorePath()).isEqualTo(trustStore);
  }

  @Test
  public void gettersAndSetters() {
    assertThat(vaultConfig.getUrl()).isEqualTo(null);
    assertThat(vaultConfig.getTlsKeyStorePath()).isEqualTo(null);
    assertThat(vaultConfig.getTlsTrustStorePath()).isEqualTo(null);

    String url = "url";
    Path keyStore = Paths.get("keystore");
    Path trustStore = Paths.get("truststore");

    vaultConfig.setUrl(url);
    vaultConfig.setTlsKeyStorePath(keyStore);
    vaultConfig.setTlsTrustStorePath(trustStore);

    assertThat(vaultConfig.getUrl()).isEqualTo(url);
    assertThat(vaultConfig.getTlsKeyStorePath()).isEqualTo(keyStore);
    assertThat(vaultConfig.getTlsTrustStorePath()).isEqualTo(trustStore);
  }

  @Test
  public void getType() {
    assertThat(vaultConfig.getKeyVaultType()).isEqualTo(KeyVaultType.HASHICORP);
  }

  @Test
  public void getApprolePathReturnsDefaultIfNotSet() {
    assertThat(vaultConfig.getApprolePath()).isEqualTo("approle");
  }

  @Test
  public void getApprolePath() {
    vaultConfig.setApprolePath("notdefault");
    assertThat(vaultConfig.getApprolePath()).isEqualTo("notdefault");
  }
}
