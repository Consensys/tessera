package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.keypairs.UnsupportedKeyPair;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.stream.Stream;

public class UnsupportedKeyPairValidator
    implements ConstraintValidator<ValidUnsupportedKeyPair, UnsupportedKeyPair> {

  @Override
  public boolean isValid(UnsupportedKeyPair keyPair, ConstraintValidatorContext context) {
    if (isIncompleteDirectKeyPair(keyPair)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "{UnsupportedKeyPair.bothDirectKeysRequired.message}")
          .addConstraintViolation();
    } else if (isIncompleteInlineKeyPair(keyPair)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "{UnsupportedKeyPair.bothInlineKeysRequired.message}")
          .addConstraintViolation();
    } else if (isIncompleteAzureVaultKeyPair(keyPair)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "{UnsupportedKeyPair.bothAzureKeysRequired.message}")
          .addConstraintViolation();
    } else if (isIncompleteHashicorpVaultKeyPair(keyPair)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}")
          .addConstraintViolation();
    } else if (isIncompleteAWSVaultKeyPair(keyPair)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAWSKeysRequired.message}")
          .addConstraintViolation();
    } else if (isIncompleteFilesystemKeyPair(keyPair)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "{UnsupportedKeyPair.bothFilesystemKeysRequired.message}")
          .addConstraintViolation();
    }

    return false;
  }

  private boolean isIncompleteDirectKeyPair(UnsupportedKeyPair keyPair) {
    return isIncomplete(keyPair.getPublicKey(), keyPair.getPrivateKey());
  }

  private boolean isIncompleteInlineKeyPair(UnsupportedKeyPair keyPair) {
    return isIncomplete(keyPair.getPublicKey(), keyPair.getConfig());
  }

  private boolean isIncompleteAzureVaultKeyPair(UnsupportedKeyPair keyPair) {
    return isIncomplete(keyPair.getAzureVaultPublicKeyId(), keyPair.getAzureVaultPrivateKeyId());
  }

  private boolean isIncompleteHashicorpVaultKeyPair(UnsupportedKeyPair keyPair) {
    return isIncomplete(
        keyPair.getHashicorpVaultPublicKeyId(),
        keyPair.getHashicorpVaultPrivateKeyId(),
        keyPair.getHashicorpVaultSecretEngineName(),
        keyPair.getHashicorpVaultSecretName());
  }

  private boolean isIncompleteAWSVaultKeyPair(UnsupportedKeyPair keyPair) {
    return isIncomplete(
        keyPair.getAwsSecretsManagerPublicKeyId(), keyPair.getAwsSecretsManagerPrivateKeyId());
  }

  private boolean isIncompleteFilesystemKeyPair(UnsupportedKeyPair keyPair) {
    return isIncomplete(keyPair.getPublicKeyPath(), keyPair.getPrivateKeyPath());
  }

  private boolean isIncomplete(Object... args) {
    return areAnyNull(args) && areAnyNonNull(args);
  }

  private boolean areAnyNull(Object... args) {
    return Stream.of(args).anyMatch(Objects::isNull);
  }

  private boolean areAnyNonNull(Object... args) {
    return Stream.of(args).anyMatch(Objects::nonNull);
  }
}
