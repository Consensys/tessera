package com.quorum.tessera.key.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.keypairs.HashicorpVaultKeyPair;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.key.vault.KeyVaultService;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class HashicorpVaultKeyGeneratorTest {

  private final String pubStr = "public";
  private final String privStr = "private";
  private final PublicKey pub = PublicKey.from(pubStr.getBytes());
  private final PrivateKey priv = PrivateKey.from(privStr.getBytes());

  private Encryptor encryptor;

  private KeyVaultService keyVaultService;

  private HashicorpVaultKeyGenerator hashicorpVaultKeyGenerator;

  @Before
  public void setUp() {
    this.encryptor = mock(Encryptor.class);
    this.keyVaultService = mock(KeyVaultService.class);

    final KeyPair keyPair = new KeyPair(pub, priv);
    when(encryptor.generateNewKeys()).thenReturn(keyPair);

    this.hashicorpVaultKeyGenerator = new HashicorpVaultKeyGenerator(encryptor, keyVaultService);
  }

  @Test(expected = NullPointerException.class)
  public void nullFilenameThrowsException() {
    KeyVaultOptions keyVaultOptions = mock(KeyVaultOptions.class);
    when(keyVaultOptions.getSecretEngineName()).thenReturn("secretEngine");

    hashicorpVaultKeyGenerator.generate(null, null, keyVaultOptions);
  }

  @Test(expected = NullPointerException.class)
  public void nullKeyVaultOptionsThrowsException() {
    hashicorpVaultKeyGenerator.generate("filename", null, null);
  }

  @Test(expected = NullPointerException.class)
  public void nullSecretEngineNameThrowsException() {
    KeyVaultOptions keyVaultOptions = mock(KeyVaultOptions.class);
    when(keyVaultOptions.getSecretEngineName()).thenReturn(null);

    hashicorpVaultKeyGenerator.generate("filename", null, keyVaultOptions);
  }

  @Test
  public void generatedKeyPairIsSavedToSpecifiedPathInVaultWithIds() {
    String secretEngine = "secretEngine";
    String filename = "secretName";

    KeyVaultOptions keyVaultOptions = mock(KeyVaultOptions.class);
    when(keyVaultOptions.getSecretEngineName()).thenReturn(secretEngine);

    HashicorpVaultKeyPair result =
        hashicorpVaultKeyGenerator.generate(filename, null, keyVaultOptions);

    HashicorpVaultKeyPair expected =
        new HashicorpVaultKeyPair("publicKey", "privateKey", secretEngine, filename, null);
    assertThat(result).isEqualToComparingFieldByField(expected);

    final ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
    verify(keyVaultService).setSecret(captor.capture());

    assertThat(captor.getAllValues()).hasSize(1);
    Map capturedArg = captor.getValue();

    Map<String, Object> expectedData = new HashMap<>();
    expectedData.put("publicKey", pub.encodeToBase64());
    expectedData.put("privateKey", priv.encodeToBase64());
    expectedData.put("secretEngineName", secretEngine);
    expectedData.put("secretName", filename);

    assertThat(capturedArg).isEqualTo(expectedData);

    verifyNoMoreInteractions(keyVaultService);
  }
}
