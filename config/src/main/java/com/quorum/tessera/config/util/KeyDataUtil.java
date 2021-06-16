package com.quorum.tessera.config.util;

import static java.util.function.Predicate.not;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keypairs.*;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.io.IOCallback;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class KeyDataUtil {

  private KeyDataUtil() {}

  public static Class<? extends ConfigKeyPair> getKeyPairTypeFor(KeyData keyData) {

    Objects.requireNonNull(keyData, "KeyData is required");

    if (isDirect(keyData)) {
      return DirectKeyPair.class;
    }

    if (isInline(keyData)) {
      return InlineKeypair.class;
    }

    if (isAzure(keyData)) {
      return AzureVaultKeyPair.class;
    }

    if (isHashicorp(keyData)) {
      return HashicorpVaultKeyPair.class;
    }

    if (isAws(keyData)) {
      return AWSKeyPair.class;
    }

    if (isFileSystem(keyData)) {
      return FilesystemKeyPair.class;
    }

    return UnsupportedKeyPair.class;
  }

  private static final Predicate<KeyData> HAS_PUBLIC_KEY = k -> Objects.nonNull(k.getPublicKey());

  public static boolean isLocked(KeyData keyData) {
    if (isFileSystem(keyData)) {
      final KeyDataConfig keyDataConfig =
          JaxbUtil.unmarshal(
              IOCallback.execute(() -> Files.newInputStream(keyData.getPrivateKeyPath())),
              KeyDataConfig.class);
      return keyDataConfig.getType() == PrivateKeyType.LOCKED;
    }

    if (Objects.isNull(keyData.getConfig())) {
      return false;
    }

    return keyData.getConfig().getType() == PrivateKeyType.LOCKED;
  }

  public static boolean isDirect(KeyData keyData) {
    return Optional.of(keyData)
        .filter(k -> Objects.nonNull(k.getPrivateKey()))
        .filter(HAS_PUBLIC_KEY)
        .isPresent();
  }

  public static boolean isInline(KeyData keyData) {
    return Optional.of(keyData)
        .filter(HAS_PUBLIC_KEY)
        .filter(k -> Objects.nonNull(k.getConfig()))
        .isPresent();
  }

  public static boolean isAzure(KeyData keyData) {
    return Optional.of(keyData)
        .filter(k -> Objects.nonNull(k.getAzureVaultPublicKeyId()))
        .filter(k -> Objects.nonNull(k.getAzureVaultPrivateKeyId()))
        .isPresent();
  }

  public static boolean isHashicorp(KeyData keyData) {

    return Stream.of(
            keyData.getHashicorpVaultPublicKeyId(),
            keyData.getHashicorpVaultPrivateKeyId(),
            keyData.getHashicorpVaultSecretEngineName(),
            keyData.getHashicorpVaultSecretName())
        .allMatch(Objects::nonNull);
  }

  public static boolean isAws(KeyData keyData) {
    return Stream.of(
            keyData.getAwsSecretsManagerPublicKeyId(), keyData.getAwsSecretsManagerPrivateKeyId())
        .allMatch(Objects::nonNull);
  }

  public static boolean isFileSystem(KeyData keyData) {
    return Stream.of(keyData.getPrivateKeyPath(), keyData.getPublicKeyPath())
        .allMatch(Objects::nonNull);
  }

  public static boolean isUnsupported(KeyData keyData) {
    return Optional.of(keyData)
        .filter(not(KeyDataUtil::isAws))
        .filter(not(KeyDataUtil::isAzure))
        .filter(not(KeyDataUtil::isDirect))
        .filter(not(KeyDataUtil::isFileSystem))
        .filter(not(KeyDataUtil::isHashicorp))
        .filter(not(KeyDataUtil::isInline))
        .isPresent();
  }

  public static ConfigKeyPair unmarshal(final KeyData keyData, final KeyEncryptor keyEncryptor) {

    // case 1, the keys are provided inline
    if (isDirect(keyData)) {
      return new DirectKeyPair(keyData.getPublicKey(), keyData.getPrivateKey());
    }

    // case 2, the config is provided inline
    if (isInline(keyData)) {
      InlineKeypair keyPair =
          new InlineKeypair(keyData.getPublicKey(), keyData.getConfig(), keyEncryptor);
      keyPair.withPassword(keyData.getPassword());
      return keyPair;
    }

    // case 3, the Azure Key Vault data is provided
    if (isAzure(keyData)) {
      return new AzureVaultKeyPair(
          keyData.getAzureVaultPublicKeyId(),
          keyData.getAzureVaultPrivateKeyId(),
          keyData.getAzureVaultPublicKeyVersion(),
          keyData.getAzureVaultPrivateKeyVersion());
    }

    // case 4, the Hashicorp Vault data is provided
    if (isHashicorp(keyData)) {

      Integer hashicorpVaultSecretVersion;

      Optional<String> hashicorpVaultSecretVersionStr =
          Optional.of(keyData).map(KeyData::getHashicorpVaultSecretVersion);

      if (hashicorpVaultSecretVersionStr.isPresent()) {
        hashicorpVaultSecretVersion =
            hashicorpVaultSecretVersionStr
                .filter(Pattern.compile("^\\d*$").asPredicate())
                .map(Integer::parseInt)
                .orElse(-1);
      } else {
        hashicorpVaultSecretVersion = 0;
      }

      return new HashicorpVaultKeyPair(
          keyData.getHashicorpVaultPublicKeyId(),
          keyData.getHashicorpVaultPrivateKeyId(),
          keyData.getHashicorpVaultSecretEngineName(),
          keyData.getHashicorpVaultSecretName(),
          hashicorpVaultSecretVersion);
    }

    // case 5, the AWS Secrets Manager data is provided
    if (isAws(keyData)) {
      return new AWSKeyPair(
          keyData.getAwsSecretsManagerPublicKeyId(), keyData.getAwsSecretsManagerPrivateKeyId());
    }

    // case 6, the keys are provided inside a file
    if (isFileSystem(keyData)) {
      FilesystemKeyPair fileSystemKeyPair =
          new FilesystemKeyPair(
              keyData.getPublicKeyPath(), keyData.getPrivateKeyPath(), keyEncryptor);
      fileSystemKeyPair.withPassword(keyData.getPassword());
      return fileSystemKeyPair;
    }

    // case 7, the key config specified is invalid
    return new UnsupportedKeyPair(
        keyData.getConfig(),
        keyData.getPrivateKey(),
        keyData.getPublicKey(),
        keyData.getPrivateKeyPath(),
        keyData.getPublicKeyPath(),
        keyData.getAzureVaultPublicKeyId(),
        keyData.getAzureVaultPrivateKeyId(),
        keyData.getAzureVaultPublicKeyVersion(),
        keyData.getAzureVaultPrivateKeyVersion(),
        keyData.getHashicorpVaultPublicKeyId(),
        keyData.getHashicorpVaultPrivateKeyId(),
        keyData.getHashicorpVaultSecretEngineName(),
        keyData.getHashicorpVaultSecretName(),
        keyData.getHashicorpVaultSecretVersion(),
        keyData.getAwsSecretsManagerPublicKeyId(),
        keyData.getAwsSecretsManagerPrivateKeyId());
  }

  public static KeyData marshal(final ConfigKeyPair keyPair) {

    final KeyData keyData = new KeyData();

    if (DirectKeyPair.class.isInstance(keyPair)) {
      DirectKeyPair kp = DirectKeyPair.class.cast(keyPair);
      keyData.setPublicKey(kp.getPublicKey());
      keyData.setPrivateKey(kp.getPrivateKey());
      return keyData;
    }

    if (InlineKeypair.class.isInstance(keyPair)) {
      InlineKeypair kp = InlineKeypair.class.cast(keyPair);
      keyData.setPublicKey(kp.getPublicKey());
      keyData.setConfig(kp.getPrivateKeyConfig());
      return keyData;
    }

    if (AzureVaultKeyPair.class.isInstance(keyPair)) {
      AzureVaultKeyPair kp = AzureVaultKeyPair.class.cast(keyPair);

      keyData.setAzureVaultPublicKeyId(kp.getPublicKeyId());
      keyData.setAzureVaultPrivateKeyId(kp.getPrivateKeyId());
      keyData.setAzureVaultPublicKeyVersion(kp.getPublicKeyVersion());
      keyData.setAzureVaultPrivateKeyVersion(kp.getPrivateKeyVersion());
      return keyData;
    }

    if (HashicorpVaultKeyPair.class.isInstance(keyPair)) {
      HashicorpVaultKeyPair kp = HashicorpVaultKeyPair.class.cast(keyPair);
      keyData.setHashicorpVaultPublicKeyId(kp.getPublicKeyId());
      keyData.setHashicorpVaultPrivateKeyId(kp.getPrivateKeyId());
      keyData.setHashicorpVaultSecretEngineName(kp.getSecretEngineName());
      keyData.setHashicorpVaultSecretName(kp.getSecretName());

      keyData.setHashicorpVaultSecretVersion(Objects.toString(kp.getSecretVersion(), null));
      return keyData;
    }

    if (AWSKeyPair.class.isInstance(keyPair)) {
      AWSKeyPair kp = AWSKeyPair.class.cast(keyPair);
      keyData.setAwsSecretsManagerPublicKeyId(kp.getPublicKeyId());
      keyData.setAwsSecretsManagerPrivateKeyId(kp.getPrivateKeyId());
      return keyData;
    }

    if (FilesystemKeyPair.class.isInstance(keyPair)) {
      FilesystemKeyPair kp = FilesystemKeyPair.class.cast(keyPair);
      keyData.setPublicKeyPath(kp.getPublicKeyPath());
      keyData.setPrivateKeyPath(kp.getPrivateKeyPath());
      return keyData;
    }

    if (UnsupportedKeyPair.class.isInstance(keyPair)) {
      UnsupportedKeyPair kp = UnsupportedKeyPair.class.cast(keyPair);
      return new KeyData(
          kp.getConfig(),
          kp.getPrivateKey(),
          kp.getPublicKey(),
          kp.getPrivateKeyPath(),
          kp.getPublicKeyPath(),
          kp.getAzureVaultPrivateKeyId(),
          kp.getAzureVaultPublicKeyId(),
          kp.getAzureVaultPublicKeyVersion(),
          kp.getAzureVaultPrivateKeyVersion(),
          kp.getHashicorpVaultPrivateKeyId(),
          kp.getHashicorpVaultPublicKeyId(),
          kp.getHashicorpVaultSecretEngineName(),
          kp.getHashicorpVaultSecretName(),
          kp.getHashicorpVaultSecretVersion(),
          kp.getAwsSecretsManagerPublicKeyId(),
          kp.getAwsSecretsManagerPrivateKeyId());
    }

    throw new UnsupportedOperationException(
        "The keypair type " + keyPair.getClass() + " is not allowed");
  }
}
