package com.quorum.tessera.config.util;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.keypairs.*;
import com.quorum.tessera.config.keys.KeyEncryptor;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class KeyDataUtil {


    public static  Class<? extends ConfigKeyPair> getKeyPairTypeFor(KeyData keyData) {
        if(isDirect(keyData)) {
            return DirectKeyPair.class;
        }

        if(isInline(keyData)) {
            return InlineKeypair.class;
        }

        if(isAzure(keyData)) {
            return AzureVaultKeyPair.class;
        }

        if(isHashicorp(keyData)) {
            return HashicorpVaultKeyPair.class;
        }

        if(isAws(keyData)) {
            return AWSKeyPair.class;
        }

        if(isFileSystem(keyData)) {
            return FilesystemKeyPair.class;
        }

        return UnsupportedKeyPair.class;

    }


    public static boolean isDirect(KeyData keyData) {
        return Objects.nonNull(keyData.getPrivateKey()) && Objects.nonNull(keyData.getPublicKey());
    }

    public static boolean isInline(KeyData keyData) {
        return Objects.nonNull(keyData.getPublicKey()) && Objects.nonNull(keyData.getConfig());
    }

    public static boolean isAzure(KeyData keyData) {
        return keyData.getAzureVaultPublicKeyId() != null && keyData.getAzureVaultPrivateKeyId() != null;
    }

    public static boolean isHashicorp(KeyData keyData) {
        return keyData.getHashicorpVaultPublicKeyId() != null
            && keyData.getHashicorpVaultPrivateKeyId() != null
            && keyData.getHashicorpVaultSecretEngineName() != null
            && keyData.getHashicorpVaultSecretName() != null;
    }

    public static boolean isAws(KeyData keyData) {
        return keyData.getAwsSecretsManagerPublicKeyId() != null && keyData.getAwsSecretsManagerPrivateKeyId() != null;
    }

    public static boolean isFileSystem(KeyData keyData) {
        return Objects.nonNull(keyData.getPrivateKey()) && Objects.nonNull(keyData.getPublicKey());
    }

    public static boolean isUnsupported(KeyData keyData) {
        return getKeyPairTypeFor(keyData).equals(UnsupportedKeyPair.class);

    }

    public static ConfigKeyPair unmarshal(final KeyData keyData, final KeyEncryptor keyEncryptor) {

        // case 1, the keys are provided inline
        if (isDirect(keyData)) {
            return new DirectKeyPair(keyData.getPublicKey(), keyData.getPrivateKey());
        }

        // case 2, the config is provided inline
        if (isInline(keyData)) {
            return new InlineKeypair(
                keyData.getPublicKey(), keyData.getConfig(), keyEncryptor);
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

            Optional<String> hashicorpVaultSecretVersionStr = Optional.of(keyData)
                .map(KeyData::getHashicorpVaultSecretVersion);

            if (hashicorpVaultSecretVersionStr.isPresent()) {
                hashicorpVaultSecretVersion = hashicorpVaultSecretVersionStr
                    .filter(Pattern.compile("^\\d*$").asPredicate())
                    .map(Integer::parseInt).orElse(-1);
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
            return new FilesystemKeyPair(keyData.getPublicKeyPath(), keyData.getPrivateKeyPath(), keyEncryptor);
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

        KeyData keyData = new KeyData();

        if (keyPair instanceof DirectKeyPair) {
            DirectKeyPair kp = (DirectKeyPair) keyPair;

            keyData.setPublicKey(kp.getPublicKey());
            keyData.setPrivateKey(kp.getPrivateKey());
            return keyData;
        }

        if (keyPair instanceof InlineKeypair) {
            InlineKeypair kp = (InlineKeypair) keyPair;

            keyData.setPublicKey(kp.getPublicKey());
            keyData.setConfig(kp.getPrivateKeyConfig());
            return keyData;
        }

        if (keyPair instanceof AzureVaultKeyPair) {
            AzureVaultKeyPair kp = (AzureVaultKeyPair) keyPair;

            keyData.setAzureVaultPublicKeyId(kp.getPublicKeyId());
            keyData.setAzureVaultPrivateKeyId(kp.getPrivateKeyId());
            keyData.setAzureVaultPublicKeyVersion(kp.getPublicKeyVersion());
            keyData.setAzureVaultPrivateKeyVersion(kp.getPrivateKeyVersion());
            return keyData;
        }

        if (keyPair instanceof HashicorpVaultKeyPair) {
            HashicorpVaultKeyPair kp = (HashicorpVaultKeyPair) keyPair;

            keyData.setHashicorpVaultPublicKeyId(kp.getPublicKeyId());
            keyData.setHashicorpVaultPrivateKeyId(kp.getPrivateKeyId());
            keyData.setHashicorpVaultSecretEngineName(kp.getSecretEngineName());
            keyData.setHashicorpVaultSecretName(kp.getSecretName());
            return keyData;
        }

        if (keyPair instanceof AWSKeyPair) {
            AWSKeyPair kp = (AWSKeyPair) keyPair;

            keyData.setAwsSecretsManagerPublicKeyId(kp.getPublicKeyId());
            keyData.setAwsSecretsManagerPrivateKeyId(kp.getPrivateKeyId());
            return keyData;
        }

        if (keyPair instanceof FilesystemKeyPair) {
            FilesystemKeyPair kp = (FilesystemKeyPair) keyPair;

            keyData.setPublicKeyPath(kp.getPublicKeyPath());
            keyData.setPrivateKeyPath(kp.getPrivateKeyPath());
            return keyData;
        }

        if (keyPair instanceof UnsupportedKeyPair) {
            UnsupportedKeyPair kp = (UnsupportedKeyPair) keyPair;
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

        throw new UnsupportedOperationException("The keypair type " + keyPair.getClass() + " is not allowed");
    }
}
