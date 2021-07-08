package com.quorum.tessera.key.vault.hashicorp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultVersionedKeyValueTemplate;
import org.springframework.vault.support.Versioned;

public class HashicorpKeyVaultServiceTest {

  private HashicorpKeyVaultService keyVaultService;

  private VaultOperations vaultOperations;

  private VaultVersionedKeyValueTemplateFactory vaultVersionedKeyValueTemplateFactory;

  @Before
  public void beforeTest() {
    this.vaultOperations = mock(VaultOperations.class);
    this.vaultVersionedKeyValueTemplateFactory = mock(VaultVersionedKeyValueTemplateFactory.class);
    this.keyVaultService =
        new HashicorpKeyVaultService(vaultOperations, () -> vaultVersionedKeyValueTemplateFactory);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(vaultOperations);
    verifyNoMoreInteractions(vaultVersionedKeyValueTemplateFactory);
  }

  @Test
  public void getSecret() {
    final Map<String, String> getSecretData =
        Map.of(
            HashicorpKeyVaultService.SECRET_ENGINE_NAME_KEY, "secretEngine",
            HashicorpKeyVaultService.SECRET_NAME_KEY, "secretName",
            HashicorpKeyVaultService.SECRET_ID_KEY, "keyId");

    Versioned versionedResponse = mock(Versioned.class);

    when(versionedResponse.hasData()).thenReturn(true);

    VaultVersionedKeyValueTemplate vaultVersionedKeyValueTemplate =
        mock(VaultVersionedKeyValueTemplate.class);
    when(vaultVersionedKeyValueTemplate.get("secretName", Versioned.Version.from(0)))
        .thenReturn(versionedResponse);

    when(vaultVersionedKeyValueTemplateFactory.createVaultVersionedKeyValueTemplate(
            vaultOperations, "secretEngine"))
        .thenReturn(vaultVersionedKeyValueTemplate);

    String keyValue = "keyvalue";
    Map responseData = Map.of("keyId", keyValue);
    when(versionedResponse.getData()).thenReturn(responseData);

    String result = keyVaultService.getSecret(getSecretData);
    assertThat(result).isEqualTo(keyValue);

    verify(vaultVersionedKeyValueTemplateFactory)
        .createVaultVersionedKeyValueTemplate(vaultOperations, "secretEngine");
  }

  @Test
  public void getSecretThrowsExceptionIfNullRetrievedFromVault() {

    Map<String, String> getSecretData =
        Map.of(
            HashicorpKeyVaultService.SECRET_ENGINE_NAME_KEY, "engine",
            HashicorpKeyVaultService.SECRET_NAME_KEY, "secretName",
            HashicorpKeyVaultService.SECRET_ID_KEY, "id",
            HashicorpKeyVaultService.SECRET_VERSION_KEY, "0");

    VaultVersionedKeyValueTemplate vaultVersionedKeyValueTemplate =
        mock(VaultVersionedKeyValueTemplate.class);
    when(vaultVersionedKeyValueTemplate.get("secretName", Versioned.Version.from(0)))
        .thenReturn(null);

    when(vaultVersionedKeyValueTemplateFactory.createVaultVersionedKeyValueTemplate(
            vaultOperations, "engine"))
        .thenReturn(vaultVersionedKeyValueTemplate);

    Throwable ex = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

    assertThat(ex).isExactlyInstanceOf(HashicorpVaultException.class);
    assertThat(ex).hasMessage("No data found at engine/secretName");

    verify(vaultVersionedKeyValueTemplateFactory)
        .createVaultVersionedKeyValueTemplate(vaultOperations, "engine");
  }

  @Test
  public void getSecretThrowsExceptionIfNoDataRetrievedFromVault() {

    final Map<String, String> getSecretData =
        Map.of(
            HashicorpKeyVaultService.SECRET_ENGINE_NAME_KEY, "engine",
            HashicorpKeyVaultService.SECRET_NAME_KEY, "secretName",
            HashicorpKeyVaultService.SECRET_ID_KEY, "id",
            HashicorpKeyVaultService.SECRET_VERSION_KEY, "0");

    Versioned versionedResponse = mock(Versioned.class);
    when(versionedResponse.hasData()).thenReturn(false);

    VaultVersionedKeyValueTemplate vaultVersionedKeyValueTemplate =
        mock(VaultVersionedKeyValueTemplate.class);
    when(vaultVersionedKeyValueTemplate.get("secretName", Versioned.Version.from(0)))
        .thenReturn(versionedResponse);

    when(vaultVersionedKeyValueTemplateFactory.createVaultVersionedKeyValueTemplate(
            vaultOperations, "engine"))
        .thenReturn(vaultVersionedKeyValueTemplate);

    Throwable ex = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

    assertThat(ex).isExactlyInstanceOf(HashicorpVaultException.class);
    assertThat(ex).hasMessage("No data found at engine/secretName");

    verify(vaultVersionedKeyValueTemplateFactory)
        .createVaultVersionedKeyValueTemplate(vaultOperations, "engine");
  }

  @Test
  public void getSecretThrowsExceptionIfValueNotFoundForGivenId() {

    final Map<String, String> getSecretData =
        Map.of(
            HashicorpKeyVaultService.SECRET_ENGINE_NAME_KEY, "engine",
            HashicorpKeyVaultService.SECRET_NAME_KEY, "secretName",
            HashicorpKeyVaultService.SECRET_ID_KEY, "id",
            HashicorpKeyVaultService.SECRET_VERSION_KEY, "0");

    Versioned versionedResponse = mock(Versioned.class);
    when(versionedResponse.hasData()).thenReturn(true);

    VaultVersionedKeyValueTemplate vaultVersionedKeyValueTemplate =
        mock(VaultVersionedKeyValueTemplate.class);
    when(vaultVersionedKeyValueTemplate.get("secretName", Versioned.Version.from(0)))
        .thenReturn(versionedResponse);

    when(vaultVersionedKeyValueTemplateFactory.createVaultVersionedKeyValueTemplate(
            vaultOperations, "engine"))
        .thenReturn(vaultVersionedKeyValueTemplate);

    Map responseData = Map.of();
    when(versionedResponse.getData()).thenReturn(responseData);

    Throwable ex = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

    assertThat(ex).isExactlyInstanceOf(HashicorpVaultException.class);
    assertThat(ex).hasMessage("No value with id id found at engine/secretName");

    verify(vaultVersionedKeyValueTemplateFactory)
        .createVaultVersionedKeyValueTemplate(vaultOperations, "engine");
  }

  @Test
  public void setSecretReturnsMetadataObject() {
    Map<String, String> setSecretData =
        Map.of(
            HashicorpKeyVaultService.SECRET_ENGINE_NAME_KEY, "engine",
            HashicorpKeyVaultService.SECRET_NAME_KEY, "name");

    Versioned.Metadata metadata = mock(Versioned.Metadata.class);
    VaultVersionedKeyValueTemplate vaultVersionedKeyValueTemplate =
        mock(VaultVersionedKeyValueTemplate.class);
    when(vaultVersionedKeyValueTemplate.put(eq("name"), anyMap())).thenReturn(metadata);

    when(vaultVersionedKeyValueTemplateFactory.createVaultVersionedKeyValueTemplate(
            vaultOperations, "engine"))
        .thenReturn(vaultVersionedKeyValueTemplate);

    Object result = keyVaultService.setSecret(setSecretData);

    assertThat(result).isInstanceOf(Versioned.Metadata.class);
    assertThat(result).isEqualTo(metadata);

    verify(vaultVersionedKeyValueTemplateFactory)
        .createVaultVersionedKeyValueTemplate(vaultOperations, "engine");
  }

  @Test
  public void setSecretIfNullPointerExceptionThenHashicorpExceptionThrown() {
    Map<String, String> setSecretData =
        Map.of(
            HashicorpKeyVaultService.SECRET_NAME_KEY, "SomeName",
            HashicorpKeyVaultService.SECRET_ENGINE_NAME_KEY, "SomeEngineName");

    when(vaultVersionedKeyValueTemplateFactory.createVaultVersionedKeyValueTemplate(
            vaultOperations, "SomeEngineName"))
        .thenReturn(null);

    Throwable ex = catchThrowable(() -> keyVaultService.setSecret(setSecretData));

    assertThat(ex).isExactlyInstanceOf(HashicorpVaultException.class);
    assertThat(ex.getMessage())
        .isEqualTo(
            "Unable to save generated secret to vault.  Ensure that the secret engine being used is a v2 kv secret engine");

    verify(vaultVersionedKeyValueTemplateFactory)
        .createVaultVersionedKeyValueTemplate(vaultOperations, "SomeEngineName");
  }
}
