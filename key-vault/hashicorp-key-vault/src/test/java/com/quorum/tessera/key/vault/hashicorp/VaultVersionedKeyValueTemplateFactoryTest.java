package com.quorum.tessera.key.vault.hashicorp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Test;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultVersionedKeyValueTemplate;

public class VaultVersionedKeyValueTemplateFactoryTest {

  @Test
  public void createVaultVersionedKeyValueTemplate() {
    VaultVersionedKeyValueTemplateFactory vaultVersionedKeyValueTemplateFactory =
        new VaultVersionedKeyValueTemplateFactory() {};

    VaultOperations vaultOperations = mock(VaultOperations.class);
    when(vaultOperations.doWithSession(any())).thenReturn(Optional.empty());
    String path = "SomeName";

    VaultVersionedKeyValueTemplate result =
        vaultVersionedKeyValueTemplateFactory.createVaultVersionedKeyValueTemplate(
            vaultOperations, path);

    assertThat(result).isNotNull();
  }
}
