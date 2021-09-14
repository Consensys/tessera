package com.quorum.tessera.config.constraints;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.keypairs.UnsupportedKeyPair;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;

public class UnsupportedKeyPairValidatorTest {

  private UnsupportedKeyPairValidator validator = new UnsupportedKeyPairValidator();

  private ConstraintValidatorContext context;

  private UnsupportedKeyPair keyPair;

  @Before
  public void setUp() {

    this.context = mock(ConstraintValidatorContext.class);
    ConstraintValidatorContext.ConstraintViolationBuilder builder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

    when(context.buildConstraintViolationWithTemplate(any(String.class))).thenReturn(builder);

    this.keyPair = new UnsupportedKeyPair();
  }

  @Test
  public void directViolationIfPublicKeyButNoPrivateKey() {
    keyPair.setPublicKey("public");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.bothDirectKeysRequired.message}");
  }

  @Test
  public void directViolationIfNoPublicKeyButPrivateKey() {
    keyPair.setPrivateKey("private");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.bothDirectKeysRequired.message}");
  }

  @Test
  public void
      directViolationIsDefaultIfNoDirectPublicEvenIfMultipleIncompleteKeyPairTypesProvided() {
    KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
    Path path = mock(Path.class);

    keyPair.setPrivateKey("private");
    keyPair.setConfig(keyDataConfig);
    keyPair.setPrivateKeyPath(path);
    keyPair.setAzureVaultPrivateKeyId("privAzure");
    keyPair.setHashicorpVaultPrivateKeyId("privHashicorp");
    keyPair.setAwsSecretsManagerPrivateKeyId(("privAWS"));

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.bothDirectKeysRequired.message}");
  }

  @Test
  public void
      directViolationIsDefaultIfNoDirectPrivateEvenIfMultipleIncompleteKeyPairTypesProvided() {
    KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
    Path path = mock(Path.class);

    keyPair.setConfig(keyDataConfig);
    keyPair.setPublicKey("public");
    keyPair.setPublicKeyPath(path);
    keyPair.setAzureVaultPublicKeyId("pubAzure");
    keyPair.setHashicorpVaultPublicKeyId("pubHashicorp");
    keyPair.setAwsSecretsManagerPublicKeyId("pubAWS");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.bothDirectKeysRequired.message}");
  }

  @Test
  public void inlineViolationIfPrivateKeyConfigButNoPublicKey() {
    KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);

    keyPair.setConfig(keyDataConfig);

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.bothInlineKeysRequired.message}");
  }

  @Test
  public void inlineViolationIfNoPublicEvenIfVaultAndFilesystemAreIncomplete() {
    KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
    Path path = mock(Path.class);

    keyPair.setConfig(keyDataConfig);
    keyPair.setPublicKeyPath(path);
    keyPair.setAzureVaultPublicKeyId("pubId");
    keyPair.setHashicorpVaultPublicKeyId("pubId");
    keyPair.setAwsSecretsManagerPublicKeyId("pubId");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.bothInlineKeysRequired.message}");
  }

  @Test
  public void azureViolationIfPublicIdButNoPrivateId() {
    keyPair.setAzureVaultPublicKeyId("pubId");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAzureKeysRequired.message}");
  }

  @Test
  public void azureViolationIfNoPublicIdButPrivateId() {
    keyPair.setAzureVaultPrivateKeyId("privId");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAzureKeysRequired.message}");
  }

  @Test
  public void azureViolationIfNoPublicIdEvenIfFilesystemIncomplete() {
    Path path = mock(Path.class);

    keyPair.setPublicKeyPath(path);
    keyPair.setAzureVaultPrivateKeyId("privId");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAzureKeysRequired.message}");
  }

  @Test
  public void hashicorpViolationIfPublicIdOnly() {
    keyPair.setHashicorpVaultPublicKeyId("pubId");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfPrivateIdOnly() {
    keyPair.setHashicorpVaultPrivateKeyId("privId");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfSecretEngineNameOnly() {
    keyPair.setHashicorpVaultSecretEngineName("secretEngineName");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfSecretNameOnly() {
    keyPair.setHashicorpVaultSecretName("secretName");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfPublicIdAndPrivateIdOnly() {
    keyPair.setHashicorpVaultPublicKeyId("pubId");
    keyPair.setHashicorpVaultPrivateKeyId("privId");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfPublicIdAndSecretEngineNameOnly() {
    keyPair.setHashicorpVaultPublicKeyId("pubId");
    keyPair.setHashicorpVaultSecretEngineName("secretEngine");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfPublicIdAndSecretNameOnly() {
    keyPair.setHashicorpVaultPublicKeyId("pubId");
    keyPair.setHashicorpVaultSecretName("secretName");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfPrivateIdAndSecretEngineNameOnly() {
    keyPair.setHashicorpVaultPrivateKeyId("privId");
    keyPair.setHashicorpVaultSecretEngineName("secretEngine");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfPrivateIdAndSecretNameOnly() {
    keyPair.setHashicorpVaultPrivateKeyId("privId");
    keyPair.setHashicorpVaultSecretName("secretName");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfSecretEngineNameAndSecretNameOnly() {
    keyPair.setHashicorpVaultSecretEngineName("secretEngine");
    keyPair.setHashicorpVaultSecretName("secretName");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfPublicIdAndPrivateIdAndSecretEngineNameOnly() {
    keyPair.setHashicorpVaultPublicKeyId("pubId");
    keyPair.setHashicorpVaultPrivateKeyId("privId");
    keyPair.setHashicorpVaultSecretEngineName("secretEngine");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfPublicIdAndPrivateIdAndSecretNameOnly() {
    keyPair.setHashicorpVaultPublicKeyId("pubId");
    keyPair.setHashicorpVaultPrivateKeyId("privId");
    keyPair.setHashicorpVaultSecretName("secretName");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfPublicIdAndSecretEngineNameAndSecretNameOnly() {
    keyPair.setHashicorpVaultPublicKeyId("pubId");
    keyPair.setHashicorpVaultSecretEngineName("secretEngine");
    keyPair.setHashicorpVaultSecretName("secretName");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void hashicorpViolationIfPrivateIdAndSecretEngineNameAndSecretNameOnly() {
    keyPair.setHashicorpVaultPrivateKeyId("privId");
    keyPair.setHashicorpVaultSecretEngineName("secretEngine");
    keyPair.setHashicorpVaultSecretName("secretName");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
  }

  @Test
  public void azureViolationIfNoPrivateIdEvenIfFilesystemIncomplete() {
    Path path = mock(Path.class);

    keyPair.setAzureVaultPublicKeyId("pubId");
    keyPair.setPublicKeyPath(path);

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAzureKeysRequired.message}");
  }

  @Test
  public void awsViolationIfPublicIdButNoPrivateId() {
    keyPair.setAwsSecretsManagerPublicKeyId("pubId");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAWSKeysRequired.message}");
  }

  @Test
  public void awsViolationIfNoPublicIdButPrivateId() {
    keyPair.setAwsSecretsManagerPrivateKeyId("privId");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAWSKeysRequired.message}");
  }

  @Test
  public void awsViolationIfNoPublicIdEvenIfFilesystemIncomplete() {
    Path path = mock(Path.class);

    keyPair.setPublicKeyPath(path);
    keyPair.setAwsSecretsManagerPrivateKeyId("privId");

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAWSKeysRequired.message}");
  }

  @Test
  public void filesystemViolationIfPublicPathButNoPrivatePath() {
    Path path = mock(Path.class);

    keyPair.setPublicKeyPath(path);

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.bothFilesystemKeysRequired.message}");
  }

  @Test
  public void filesystemViolationIfNoPublicPathButPrivatePath() {
    Path path = mock(Path.class);

    keyPair.setPrivateKeyPath(path);

    validator.isValid(keyPair, context);

    verify(context)
        .buildConstraintViolationWithTemplate(
            "{UnsupportedKeyPair.bothFilesystemKeysRequired.message}");
  }

  @Test
  public void defaultViolationIfNoRecognisedKeyPairDataProvided() {
    // nothing set
    validator.isValid(keyPair, context);

    verifyNoMoreInteractions(context);
  }
}
