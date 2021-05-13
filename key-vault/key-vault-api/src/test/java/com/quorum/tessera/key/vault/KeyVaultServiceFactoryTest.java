package com.quorum.tessera.key.vault;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.KeyVaultType;
import org.junit.Test;

public class KeyVaultServiceFactoryTest {

  @Test
  public void getInstance() {
    KeyVaultServiceFactory keyVaultServiceFactory =
        KeyVaultServiceFactory.getInstance(KeyVaultType.AZURE);

    assertThat(keyVaultServiceFactory).isExactlyInstanceOf(MockAzureKeyVaultServiceFactory.class);
  }

  @Test(expected = NoKeyVaultServiceFactoryException.class)
  public void instanceNotFound() {
    KeyVaultServiceFactory.getInstance(null);
  }
}
