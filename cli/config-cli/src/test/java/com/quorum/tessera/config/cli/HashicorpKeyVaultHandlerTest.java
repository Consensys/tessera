package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.HashicorpKeyVaultConfig;
import com.quorum.tessera.config.KeyVaultConfig;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;

public class HashicorpKeyVaultHandlerTest {

  private HashicorpKeyVaultHandler keyVaultHandler;

  @Before
  public void beforeTest() {
    keyVaultHandler = new HashicorpKeyVaultHandler();
  }

  @Test
  public void handleNullOptions() {
    KeyVaultConfig result = keyVaultHandler.handle(null);
    assertThat(result).isNotNull().isExactlyInstanceOf(HashicorpKeyVaultConfig.class);
  }

  @Test
  public void handleEmptyOptions() {
    KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
    HashicorpKeyVaultConfig result =
        (HashicorpKeyVaultConfig) keyVaultHandler.handle(keyVaultConfigOptions);
    assertThat(result).isNotNull();
    assertThat(result.getApprolePath()).isEqualTo("approle");
    assertThat(result.getUrl()).isNull();
    assertThat(result.getTlsKeyStorePath()).isNull();
    assertThat(result.getTlsTrustStorePath()).isNull();

    verify(keyVaultConfigOptions).getVaultUrl();
    verify(keyVaultConfigOptions).getHashicorpApprolePath();
    verify(keyVaultConfigOptions).getHashicorpTlsKeystore();
    verify(keyVaultConfigOptions).getHashicorpTlsTruststore();
    verifyNoMoreInteractions(keyVaultConfigOptions);
  }

  @Test
  public void handle() {

    final KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
    final String vaultUrl = "vaultUrl";
    final String providedApprolePath = "providedApprolePath";

    final Path tlsKeystorePath = mock(Path.class);
    final Path tlsTrustStorePath = mock(Path.class);

    when(keyVaultConfigOptions.getVaultUrl()).thenReturn(vaultUrl);
    when(keyVaultConfigOptions.getHashicorpApprolePath()).thenReturn(providedApprolePath);
    when(keyVaultConfigOptions.getHashicorpTlsKeystore()).thenReturn(tlsKeystorePath);
    when(keyVaultConfigOptions.getHashicorpTlsTruststore()).thenReturn(tlsTrustStorePath);

    HashicorpKeyVaultConfig result =
        (HashicorpKeyVaultConfig) keyVaultHandler.handle(keyVaultConfigOptions);
    assertThat(result).isNotNull();
    assertThat(result.getApprolePath()).isEqualTo(providedApprolePath);
    assertThat(result.getUrl()).isEqualTo(vaultUrl);
    assertThat(result.getTlsKeyStorePath()).isEqualTo(tlsKeystorePath);
    assertThat(result.getTlsTrustStorePath()).isEqualTo(tlsTrustStorePath);

    verify(keyVaultConfigOptions).getVaultUrl();
    verify(keyVaultConfigOptions).getHashicorpApprolePath();
    verify(keyVaultConfigOptions).getHashicorpTlsKeystore();
    verify(keyVaultConfigOptions).getHashicorpTlsTruststore();
    verifyNoMoreInteractions(keyVaultConfigOptions);
  }
}
