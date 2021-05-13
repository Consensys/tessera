package com.quorum.tessera.config.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keypairs.*;
import com.quorum.tessera.config.keys.KeyEncryptor;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class KeyDataUtilTest {

  @Test
  public void getKeyPairTypeForDirectKeyPair() {
    KeyData keyData = new KeyData();
    keyData.setPrivateKey("PRIVATE_KEY");
    keyData.setPublicKey("PUBLIC_KEY");

    Class<? extends ConfigKeyPair> result = KeyDataUtil.getKeyPairTypeFor(keyData);
    assertThat(result).isEqualTo(DirectKeyPair.class);
  }

  @Test
  public void getKeyPairTypeForInlineKeyPair() {
    KeyData keyData = new KeyData();
    keyData.setPublicKey("PUBLIC_KEY");
    keyData.setConfig(new KeyDataConfig());

    Class<? extends ConfigKeyPair> result = KeyDataUtil.getKeyPairTypeFor(keyData);
    assertThat(result).isEqualTo(InlineKeypair.class);
  }

  @Test
  public void getKeyPairTypeForAzureVaultKeyPair() {
    KeyData keyData = new KeyData();
    keyData.setAzureVaultPublicKeyId("AZURE_PUBLIC_ID");
    keyData.setAzureVaultPrivateKeyId("AZURE_PRIVATE_ID");

    Class<? extends ConfigKeyPair> result = KeyDataUtil.getKeyPairTypeFor(keyData);
    assertThat(result).isEqualTo(AzureVaultKeyPair.class);
  }

  @Test
  public void getKeyPairTypeForHashicorpVaultKeyPair() {
    KeyData keyData = new KeyData();
    keyData.setHashicorpVaultSecretName("HASHICORP_SECRET_NAME");
    keyData.setHashicorpVaultSecretEngineName("HASHICORP_VAULT_ENGINE");
    keyData.setHashicorpVaultPrivateKeyId("HASHICORP_PRIVATE_ID");
    keyData.setHashicorpVaultPublicKeyId("HASHICORP_PUBLIC_ID");

    Class<? extends ConfigKeyPair> result = KeyDataUtil.getKeyPairTypeFor(keyData);
    assertThat(result).isEqualTo(HashicorpVaultKeyPair.class);
  }

  @Test
  public void getKeyPairTypeForAwsVaultKeyPair() {
    KeyData keyData = new KeyData();
    keyData.setAwsSecretsManagerPrivateKeyId("AWS_PRIVATE_KEY");
    keyData.setAwsSecretsManagerPublicKeyId("AWS_PUBLIC_KEY");

    Class<? extends ConfigKeyPair> result = KeyDataUtil.getKeyPairTypeFor(keyData);
    assertThat(result).isEqualTo(AWSKeyPair.class);
  }

  @Test
  public void getKeyPairTypeForFileSystemKeyPair() {
    KeyData keyData = new KeyData();
    keyData.setPublicKeyPath(mock(Path.class));
    keyData.setPrivateKeyPath(mock(Path.class));

    Class<? extends ConfigKeyPair> result = KeyDataUtil.getKeyPairTypeFor(keyData);

    assertThat(result).isEqualTo(FilesystemKeyPair.class);
  }

  @Test
  public void marshalAWSKeyPair() {
    AWSKeyPair configKeyPair = new AWSKeyPair("PUBLIC_KEY_ID", "PRIVATE_KEY_ID");
    KeyData result = KeyDataUtil.marshal(configKeyPair);

    assertThat(result.getAwsSecretsManagerPublicKeyId()).isEqualTo("PUBLIC_KEY_ID");
    assertThat(result.getAwsSecretsManagerPrivateKeyId()).isEqualTo("PRIVATE_KEY_ID");
  }

  @Test
  public void marshalAzureVaultKeyPair() {
    AzureVaultKeyPair configKeyPair =
        new AzureVaultKeyPair(
            "PUBLIC_KEY_ID", "PRIVATE_KEY_ID", "PUBLIC_KEY_VERSION", "PRIVATE_KEY_VERSION");
    KeyData result = KeyDataUtil.marshal(configKeyPair);

    assertThat(result.getAzureVaultPrivateKeyId()).isEqualTo("PRIVATE_KEY_ID");
    assertThat(result.getAzureVaultPublicKeyId()).isEqualTo("PUBLIC_KEY_ID");
    assertThat(result.getAzureVaultPrivateKeyVersion()).isEqualTo("PRIVATE_KEY_VERSION");
    assertThat(result.getAzureVaultPublicKeyVersion()).isEqualTo("PUBLIC_KEY_VERSION");
  }

  @Test
  public void marshalDirectKeyPair() {
    DirectKeyPair directKeyPair = new DirectKeyPair("PUBLIC_KEY", "PRIVATE_KEY");
    KeyData result = KeyDataUtil.marshal(directKeyPair);

    assertThat(result.getPublicKey()).isEqualTo("PUBLIC_KEY");
    assertThat(result.getPrivateKey()).isEqualTo("PRIVATE_KEY");
  }

  @Test
  public void marshalInlineKeypair() {

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
    KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
    InlineKeypair keyPair = new InlineKeypair("PUBLIC_KEY", keyDataConfig, keyEncryptor);
    KeyData result = KeyDataUtil.marshal(keyPair);

    assertThat(result.getPublicKey()).isEqualTo("PUBLIC_KEY");
    assertThat(result.getConfig()).isSameAs(keyDataConfig);
  }

  @Test
  public void marshalAwsKeypair() {
    AWSKeyPair keyPair =
        new AWSKeyPair("AwsSecretsManagerPublicKeyId", "AwsSecretsManagerPrivateKeyId");
    KeyData result = KeyDataUtil.marshal(keyPair);

    assertThat(result.getAwsSecretsManagerPublicKeyId()).isEqualTo("AwsSecretsManagerPublicKeyId");
    assertThat(result.getAwsSecretsManagerPrivateKeyId())
        .isEqualTo("AwsSecretsManagerPrivateKeyId");
  }

  @Test
  public void marshalHashicorpVaultKeyPair() {
    HashicorpVaultKeyPair keyPair =
        new HashicorpVaultKeyPair(
            "HashicorpVaultPublicKeyId",
            "HashicorpVaultPrivateKeyId",
            "HashicorpVaultSecretEngineName",
            "HashicorpVaultSecretName",
            1);
    KeyData result = KeyDataUtil.marshal(keyPair);
    assertThat(result.getHashicorpVaultPublicKeyId()).isEqualTo("HashicorpVaultPublicKeyId");
    assertThat(result.getHashicorpVaultPrivateKeyId()).isEqualTo("HashicorpVaultPrivateKeyId");
    assertThat(result.getHashicorpVaultSecretEngineName())
        .isEqualTo("HashicorpVaultSecretEngineName");
    assertThat(result.getHashicorpVaultSecretName()).isEqualTo("HashicorpVaultSecretName");
    assertThat(result.getHashicorpVaultSecretVersion()).isEqualTo("1");
  }

  @Test
  public void marshalFilesystemKeyPair() {
    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
    Path pubicKeyPath = mock(Path.class);
    Path privateKeyPath = mock(Path.class);
    FilesystemKeyPair keyPair = new FilesystemKeyPair(pubicKeyPath, privateKeyPath, keyEncryptor);
    KeyData result = KeyDataUtil.marshal(keyPair);

    assertThat(result.getPublicKeyPath()).isSameAs(pubicKeyPath);
    assertThat(result.getPrivateKeyPath()).isSameAs(privateKeyPath);
  }

  @Test
  public void marshalUnsupportedKeyPair() {
    UnsupportedKeyPair keyPair = new UnsupportedKeyPair();
    KeyData result = KeyDataUtil.marshal(keyPair);
    assertThat(result).isNotNull();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void marshalUnknownKeyPair() {
    ConfigKeyPair keyPair = mock(ConfigKeyPair.class);
    KeyDataUtil.marshal(keyPair);
  }

  @Test
  public void unmarshalDirectKeyPair() {

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
    KeyData keyData = new KeyData();
    keyData.setPublicKey("PUBLICKEY");
    keyData.setPrivateKey("PRIVATEKEY");
    ConfigKeyPair result = KeyDataUtil.unmarshal(keyData, keyEncryptor);
    assertThat(result).isNotNull().isExactlyInstanceOf(DirectKeyPair.class);
    assertThat(result.getPublicKey()).isEqualTo("PUBLICKEY");
    assertThat(result.getPrivateKey()).isEqualTo("PRIVATEKEY");

    verifyZeroInteractions(keyEncryptor);
  }

  @Test
  public void unmarshalInlineKeypair() {

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
    KeyData keyData = new KeyData();
    keyData.setPublicKey("PUBLICKEY");
    KeyDataConfig config = mock(KeyDataConfig.class);
    keyData.setConfig(config);

    InlineKeypair result = (InlineKeypair) KeyDataUtil.unmarshal(keyData, keyEncryptor);

    assertThat(result).isNotNull();

    assertThat(result.getPublicKey()).isEqualTo("PUBLICKEY");
    assertThat(result.getPrivateKeyConfig()).isSameAs(config);

    verifyZeroInteractions(keyEncryptor);
  }

  @Test
  public void unmarshalAzureVaultKeyPair() {

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
    KeyData keyData = new KeyData();
    keyData.setAzureVaultPrivateKeyId("AzureVaultPrivateKeyId");
    keyData.setAzureVaultPublicKeyId("AzureVaultPublicKeyId");

    KeyDataConfig config = mock(KeyDataConfig.class);
    keyData.setConfig(config);

    AzureVaultKeyPair result = (AzureVaultKeyPair) KeyDataUtil.unmarshal(keyData, keyEncryptor);

    assertThat(result).isNotNull();

    assertThat(result.getPrivateKeyId()).isEqualTo("AzureVaultPrivateKeyId");
    assertThat(result.getPublicKeyId()).isEqualTo("AzureVaultPublicKeyId");

    verifyZeroInteractions(keyEncryptor);
  }

  @Test
  public void unmarshalHashicorpVaultKeyPair() {

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
    KeyData keyData = new KeyData();
    keyData.setHashicorpVaultPrivateKeyId("HashicorpVaultPrivateKeyId");
    keyData.setHashicorpVaultPublicKeyId("HashicorpVaultPublicKeyId");
    keyData.setHashicorpVaultSecretVersion("99");
    keyData.setHashicorpVaultSecretName("HashicorpSecretName");
    keyData.setHashicorpVaultSecretEngineName("HashicorpVaultSecretEngineName");

    HashicorpVaultKeyPair result =
        (HashicorpVaultKeyPair) KeyDataUtil.unmarshal(keyData, keyEncryptor);

    assertThat(result).isNotNull();

    assertThat(result.getPrivateKeyId()).isEqualTo("HashicorpVaultPrivateKeyId");
    assertThat(result.getPublicKeyId()).isEqualTo("HashicorpVaultPublicKeyId");
    assertThat(result.getSecretVersion()).isEqualTo(99);
    assertThat(result.getSecretEngineName()).isEqualTo("HashicorpVaultSecretEngineName");
    assertThat(result.getSecretName()).isEqualTo("HashicorpSecretName");

    verifyZeroInteractions(keyEncryptor);
  }

  @Test
  public void unmarshalHashicorpVaultKeyPairNoSecretVersionDefined() {

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
    KeyData keyData = new KeyData();
    keyData.setHashicorpVaultPrivateKeyId("HashicorpVaultPrivateKeyId");
    keyData.setHashicorpVaultPublicKeyId("HashicorpVaultPublicKeyId");
    keyData.setHashicorpVaultSecretName("HashicorpSecretName");
    keyData.setHashicorpVaultSecretEngineName("HashicorpVaultSecretEngineName");

    HashicorpVaultKeyPair result =
        (HashicorpVaultKeyPair) KeyDataUtil.unmarshal(keyData, keyEncryptor);

    assertThat(result).isNotNull();

    assertThat(result.getPrivateKeyId()).isEqualTo("HashicorpVaultPrivateKeyId");
    assertThat(result.getPublicKeyId()).isEqualTo("HashicorpVaultPublicKeyId");
    assertThat(result.getSecretVersion()).isZero();
    assertThat(result.getSecretEngineName()).isEqualTo("HashicorpVaultSecretEngineName");
    assertThat(result.getSecretName()).isEqualTo("HashicorpSecretName");

    verifyZeroInteractions(keyEncryptor);
  }

  @Test
  public void unmarshalAwsVaultKeyPair() {

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
    KeyData keyData = new KeyData();
    keyData.setAwsSecretsManagerPrivateKeyId("AwsSecretsManagerPrivateKeyId");
    keyData.setAwsSecretsManagerPublicKeyId("AwsSecretsManagerPublicKeyId");

    AWSKeyPair result = (AWSKeyPair) KeyDataUtil.unmarshal(keyData, keyEncryptor);

    assertThat(result).isNotNull();

    assertThat(result.getPrivateKeyId()).isEqualTo("AwsSecretsManagerPrivateKeyId");
    assertThat(result.getPublicKeyId()).isEqualTo("AwsSecretsManagerPublicKeyId");

    verifyZeroInteractions(keyEncryptor);
  }

  @Test
  public void unmarshalFilesystemKeyPair() {

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
    KeyData keyData = new KeyData();
    Path privateKeyPath = mock(Path.class);
    Path publicKeyPath = mock(Path.class);

    keyData.setPrivateKeyPath(privateKeyPath);
    keyData.setPublicKeyPath(publicKeyPath);

    FilesystemKeyPair result = (FilesystemKeyPair) KeyDataUtil.unmarshal(keyData, keyEncryptor);

    assertThat(result).isNotNull();

    assertThat(result.getPrivateKeyPath()).isSameAs(privateKeyPath);
    assertThat(result.getPublicKeyPath()).isSameAs(publicKeyPath);

    verifyZeroInteractions(keyEncryptor);
  }

  @Test
  public void unmarshalUnknownKeyPair() {

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
    KeyData keyData = new KeyData();

    UnsupportedKeyPair result = (UnsupportedKeyPair) KeyDataUtil.unmarshal(keyData, keyEncryptor);

    assertThat(result).isNotNull();

    verifyZeroInteractions(keyEncryptor);
  }

  @Test
  public void isDirect() {
    KeyData keyData = new KeyData();
    keyData.setPublicKey("PUBLICKEY");
    keyData.setPrivateKey("PRIVATEKEY");
    assertThat(KeyDataUtil.isDirect(keyData)).isTrue();
  }

  @Test
  public void isInline() {
    KeyData keyData = new KeyData();
    keyData.setPublicKey("PUBLICKEY");
    keyData.setConfig(mock(KeyDataConfig.class));
    assertThat(KeyDataUtil.isInline(keyData)).isTrue();
  }

  @Test
  public void isFilesystem() {
    KeyData keyData = new KeyData();
    keyData.setPublicKeyPath(mock(Path.class));
    keyData.setPrivateKeyPath(mock(Path.class));
    assertThat(KeyDataUtil.isFileSystem(keyData)).isTrue();
  }

  @Test
  public void isAzure() {
    KeyData keyData = new KeyData();
    keyData.setAzureVaultPrivateKeyId("AzureVaultPrivateKeyId");
    keyData.setAzureVaultPublicKeyId("AzureVaultPublicKeyId");

    assertThat(KeyDataUtil.isAzure(keyData)).isTrue();
  }

  @Test
  public void isHashicorp() {
    KeyData keyData = new KeyData();
    keyData.setHashicorpVaultPublicKeyId("HashicorpVaultPublicKeyId");
    keyData.setHashicorpVaultPrivateKeyId("HashicorpVaultPrivateKeyId");
    keyData.setHashicorpVaultSecretName("HashicorpVaultSecretName");
    keyData.setHashicorpVaultSecretEngineName("HashicorpVaultSecretEngineName");

    assertThat(KeyDataUtil.isHashicorp(keyData)).isTrue();
  }

  @Test
  public void isUnsupported() {
    KeyData keyData = new KeyData();
    assertThat(KeyDataUtil.isUnsupported(keyData)).isTrue();
  }

  @Test
  public void isLockedFromFileSystem() throws URISyntaxException {

    KeyData keyData = new KeyData();
    keyData.setPublicKeyPath(mock(Path.class));
    final Path privUnlockedFile =
        Paths.get(getClass().getResource("/unlockedprivatekey.json").toURI());
    keyData.setPrivateKeyPath(privUnlockedFile);

    assertThat(KeyDataUtil.isLocked(keyData)).isFalse();

    final Path privLockedFile = Paths.get(getClass().getResource("/lockedprivatekey.json").toURI());
    keyData.setPrivateKeyPath(privLockedFile);

    assertThat(KeyDataUtil.isLocked(keyData)).isTrue();
  }

  @Test
  public void isLocked() {
    KeyData keyData = new KeyData();
    keyData.setPublicKey("PUBLIC_KEY");
    KeyDataConfig keyDataConfig =
        new KeyDataConfig(mock(PrivateKeyData.class), PrivateKeyType.LOCKED);
    keyData.setConfig(keyDataConfig);
    assertThat(KeyDataUtil.isLocked(keyData)).isTrue();

    keyData.setConfig(null);
    assertThat(KeyDataUtil.isLocked(keyData)).isFalse();

    keyData.setConfig(new KeyDataConfig());
    assertThat(KeyDataUtil.isLocked(keyData)).isFalse();
  }
}
