package com.quorum.tessera.key.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.key.vault.KeyVaultService;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AzureVaultKeyGeneratorTest {

  private final String pubStr = "public";
  private final String privStr = "private";
  private final PublicKey pub = PublicKey.from(pubStr.getBytes());
  private final PrivateKey priv = PrivateKey.from(privStr.getBytes());

  private Encryptor encryptor;
  private KeyVaultService keyVaultService;
  private AzureVaultKeyGenerator azureVaultKeyGenerator;

  @Before
  public void setUp() {
    this.encryptor = mock(Encryptor.class);
    this.keyVaultService = mock(KeyVaultService.class);

    final KeyPair keyPair = new KeyPair(pub, priv);

    when(encryptor.generateNewKeys()).thenReturn(keyPair);

    azureVaultKeyGenerator = new AzureVaultKeyGenerator(encryptor, keyVaultService);
  }

  @Test
  public void keysSavedInVaultWithProvidedVaultIdAndCorrectSuffix() {
    final String vaultId = "vaultId";
    final String pubVaultId = vaultId + "Pub";
    final String privVaultId = vaultId + "Key";

    final AzureVaultKeyPair result = azureVaultKeyGenerator.generate(vaultId, null, null);

    final ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);

    verify(keyVaultService, times(2)).setSecret(captor.capture());

    List<Map> capturedArgs = captor.getAllValues();
    assertThat(capturedArgs).hasSize(2);

    Map<String, String> expectedDataPub =
        Map.of("secretName", pubVaultId, "secret", pub.encodeToBase64());

    Map<String, String> expectedDataPriv =
        Map.of("secretName", privVaultId, "secret", priv.encodeToBase64());

    assertThat(capturedArgs)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(expectedDataPub, expectedDataPriv);

    verifyNoMoreInteractions(keyVaultService);

    final AzureVaultKeyPair expected = new AzureVaultKeyPair(pubVaultId, privVaultId, null, null);

    assertThat(result).isEqualToComparingFieldByFieldRecursively(expected);
  }

  @Test
  public void vaultIdIsFinalComponentOfFilePath() {
    final String vaultId = "vaultId";
    final String pubVaultId = vaultId + "Pub";
    final String privVaultId = vaultId + "Key";
    final String path = "/some/path/" + vaultId;

    azureVaultKeyGenerator.generate(path, null, null);

    final ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);

    verify(keyVaultService, times(2)).setSecret(captor.capture());

    List<Map> capturedArgs = captor.getAllValues();
    assertThat(capturedArgs).hasSize(2);

    Map<String, String> expectedDataPub =
        Map.of("secretName", pubVaultId, "secret", pub.encodeToBase64());

    Map<String, String> expectedDataPriv =
        Map.of("secretName", privVaultId, "secret", priv.encodeToBase64());

    assertThat(capturedArgs)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(expectedDataPub, expectedDataPriv);

    verifyNoMoreInteractions(keyVaultService);
  }

  @Test
  public void ifNoVaultIdProvidedThenSuffixOnlyIsUsed() {
    azureVaultKeyGenerator.generate(null, null, null);

    final ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);

    verify(keyVaultService, times(2)).setSecret(captor.capture());

    List<Map> capturedArgs = captor.getAllValues();
    assertThat(capturedArgs).hasSize(2);

    Map<String, String> expectedDataPub =
        Map.of("secretName", "Pub", "secret", pub.encodeToBase64());

    Map<String, String> expectedDataPriv =
        Map.of("secretName", "Key", "secret", priv.encodeToBase64());
    assertThat(capturedArgs)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(expectedDataPub, expectedDataPriv);

    verifyNoMoreInteractions(keyVaultService);
  }

  @Test
  public void allowedCharactersUsedInVaultIdDoesNotThrowException() {
    final String allowedId = "abcdefghijklmnopqrstuvwxyz-ABCDEFDGHIJKLMNOPQRSTUVWXYZ-0123456789";

    azureVaultKeyGenerator.generate(allowedId, null, null);

    verify(keyVaultService, times(2)).setSecret(any(Map.class));
  }

  @Test
  public void exceptionThrownIfDisallowedCharactersUsedInVaultId() {
    final String invalidId = "/tmp/abc@+";

    final Throwable throwable =
        catchThrowable(() -> azureVaultKeyGenerator.generate(invalidId, null, null));

    assertThat(throwable).isInstanceOf(UnsupportedCharsetException.class);
    assertThat(throwable)
        .hasMessageContaining(
            "Generated key ID for Azure Key Vault can contain only 0-9, a-z, A-Z and - characters");
  }

  @Test
  public void encryptionIsNotUsedWhenSavingToVault() {
    final ArgonOptions argonOptions = mock(ArgonOptions.class);

    azureVaultKeyGenerator.generate("vaultId", argonOptions, null);

    verifyNoMoreInteractions(argonOptions);
  }
}
