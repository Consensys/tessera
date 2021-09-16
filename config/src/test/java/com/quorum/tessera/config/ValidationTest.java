package com.quorum.tessera.config;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.config.keypairs.InlineKeypair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;

public class ValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);

  @Test
  public void validateArgonOptions() {
    ArgonOptions options = new ArgonOptions("d", 10, 20, 30);

    Set<ConstraintViolation<ArgonOptions>> violations = validator.validate(options);

    assertThat(violations).isEmpty();
  }

  @Test
  public void validateArgonOptionsInvalidAlgo() {
    ArgonOptions options = new ArgonOptions("a", 10, 20, 30);

    Set<ConstraintViolation<ArgonOptions>> violations = validator.validate(options);

    assertThat(violations).hasSize(1);
  }

  @Test
  public void validateArgonOptionsAllNullAlgoHasDefaultValue() {
    ArgonOptions options = new ArgonOptions(null, null, null, null);

    Set<ConstraintViolation<ArgonOptions>> violations = validator.validate(options);

    assertThat(violations).hasSize(3);
    assertThat(options.getAlgorithm()).isEqualTo("id");
  }

  @Test
  public void inlineKeyPairNaClFailure() {

    KeyDataConfig keyConfig = mock(KeyDataConfig.class);
    when(keyConfig.getType()).thenReturn(PrivateKeyType.UNLOCKED);
    when(keyConfig.getValue()).thenReturn("NACL_FAILURE");

    InlineKeypair keyPair = new InlineKeypair("validkey", keyConfig, keyEncryptor);

    Set<ConstraintViolation<InlineKeypair>> violations = validator.validate(keyPair);

    assertThat(violations).hasSize(1);

    ConstraintViolation<InlineKeypair> violation = violations.iterator().next();

    assertThat(violation.getMessageTemplate())
        .isEqualTo(
            "Could not decrypt the private key with the provided password, please double check the passwords provided");
  }

  @Test
  public void directKeyPairInvalidBase64() {
    DirectKeyPair keyPair = new DirectKeyPair("INVALID_BASE", "INVALID_BASE");

    Set<ConstraintViolation<DirectKeyPair>> violations = validator.validate(keyPair);

    assertThat(violations).hasSize(2);

    Iterator<ConstraintViolation<DirectKeyPair>> iterator = violations.iterator();
    ConstraintViolation<DirectKeyPair> violation = iterator.next();

    assertThat(violation.getMessageTemplate()).isEqualTo("Invalid Base64 key provided");

    ConstraintViolation<DirectKeyPair> violation2 = iterator.next();

    assertThat(violation2.getMessageTemplate()).isEqualTo("Invalid Base64 key provided");
  }

  @Test
  public void inlineKeyPairInvalidBase64() {
    KeyDataConfig keyConfig = mock(KeyDataConfig.class);
    when(keyConfig.getType()).thenReturn(PrivateKeyType.UNLOCKED);
    when(keyConfig.getValue()).thenReturn("validkey");
    InlineKeypair keyPair = new InlineKeypair("INVALID_BASE", keyConfig, keyEncryptor);

    Set<ConstraintViolation<InlineKeypair>> violations = validator.validate(keyPair);

    assertThat(violations).hasSize(1);

    ConstraintViolation<InlineKeypair> violation = violations.iterator().next();

    assertThat(violation.getMessageTemplate()).isEqualTo("Invalid Base64 key provided");
    assertThat(violation.getPropertyPath().toString()).isEqualTo("publicKey");
  }

  @Test
  public void invalidAlwaysSendTo() {

    List<String> alwaysSendTo = singletonList("BOGUS");

    Config config = new Config(null, null, null, null, alwaysSendTo, false, false);

    Set<ConstraintViolation<Config>> violations =
        validator.validateProperty(config, "alwaysSendTo");

    assertThat(violations).hasSize(1);

    ConstraintViolation<Config> violation = violations.iterator().next();
    assertThat(violation.getPropertyPath().toString()).startsWith("alwaysSendTo[0]");
    assertThat(violation.getMessageTemplate()).isEqualTo("{ValidBase64.message}");
  }

  @Test
  public void validAlwaysSendTo() {

    String value = Base64.getEncoder().encodeToString("HELLOW".getBytes());

    List<String> alwaysSendTo = singletonList(value);

    Config config = new Config(null, null, null, null, alwaysSendTo, false, false);

    Set<ConstraintViolation<Config>> violations =
        validator.validateProperty(config, "alwaysSendTo");

    assertThat(violations).isEmpty();
  }

  @Test
  public void azureKeyPairIdsAllowedCharacterSetIsAlphanumericAndDash() {
    String keyVaultId = "0123456789-abcdefghijklmnopqrstuvwxyz-ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    AzureVaultKeyPair keyPair = new AzureVaultKeyPair(keyVaultId, keyVaultId, null, null);

    Set<ConstraintViolation<AzureVaultKeyPair>> violations = validator.validate(keyPair);
    assertThat(violations).hasSize(0);
  }

  @Test
  public void azureKeyPairIdsDisallowedCharactersCreateViolation() {
    String keyVaultId = "invalid_@!£$%^~^&_id";
    AzureVaultKeyPair keyPair = new AzureVaultKeyPair(keyVaultId, keyVaultId, null, null);

    Set<ConstraintViolation<AzureVaultKeyPair>> violations = validator.validate(keyPair);
    assertThat(violations).hasSize(2);

    assertThat(violations)
        .extracting("messageTemplate")
        .containsExactly(
            "Azure Key Vault key IDs can only contain alphanumeric characters and dashes (-)",
            "Azure Key Vault key IDs can only contain alphanumeric characters and dashes (-)");
  }

  @Test
  public void azureKeyPairKeyVersionMustBe32CharsLong() {
    String is32Chars = "12345678901234567890123456789012";
    AzureVaultKeyPair keyPair = new AzureVaultKeyPair("id", "id", is32Chars, is32Chars);

    Set<ConstraintViolation<AzureVaultKeyPair>> violations = validator.validate(keyPair);
    assertThat(violations).hasSize(0);
  }

  @Test
  public void azureKeyPairKeyVersionLongerThan32CharsCreatesViolation() {
    String is33Chars = "123456789012345678901234567890123";
    AzureVaultKeyPair keyPair = new AzureVaultKeyPair("id", "id", is33Chars, is33Chars);

    Set<ConstraintViolation<AzureVaultKeyPair>> violations = validator.validate(keyPair);
    assertThat(violations).hasSize(2);

    assertThat(violations)
        .extracting("messageTemplate")
        .containsExactly("length must be 32 characters", "length must be 32 characters");
  }

  @Test
  public void azureKeyPairKeyVersionShorterThan32CharsCreatesViolation() {
    String is31Chars = "1234567890123456789012345678901";
    AzureVaultKeyPair keyPair = new AzureVaultKeyPair("id", "id", is31Chars, is31Chars);

    Set<ConstraintViolation<AzureVaultKeyPair>> violations = validator.validate(keyPair);
    assertThat(violations).hasSize(2);

    assertThat(violations)
        .extracting("messageTemplate")
        .containsExactly("length must be 32 characters", "length must be 32 characters");
  }

  @Test
  public void azureKeyPairOnlyPublicKeyVersionSetCreatesViolation() {
    String is32Chars = "12345678901234567890123456789012";

    AzureVaultKeyPair azureVaultKeyPair = new AzureVaultKeyPair("pubId", "privId", is32Chars, null);

    Set<ConstraintViolation<AzureVaultKeyPair>> violations = validator.validate(azureVaultKeyPair);
    assertThat(violations).hasSize(1);

    assertThat(violations.iterator().next().getMessage())
        .isEqualTo(
            "Only one key version was provided for the Azure vault key pair.  Either set the version for both the public and private key, or leave both unset");
  }

  @Test
  public void azureKeyPairOnlyPrivateKeyVersionSetCreatesViolation() {
    String is32Chars = "12345678901234567890123456789012";

    AzureVaultKeyPair azureVaultKeyPair = new AzureVaultKeyPair("pubId", "privId", null, is32Chars);

    Set<ConstraintViolation<AzureVaultKeyPair>> violations = validator.validate(azureVaultKeyPair);
    assertThat(violations).hasSize(1);

    assertThat(violations.iterator().next().getMessage())
        .isEqualTo(
            "Only one key version was provided for the Azure vault key pair.  Either set the version for both the public and private key, or leave both unset");
  }

  @Test
  public void azureVaultConfigWithNoUrlCreatesNullViolation() {
    AzureKeyVaultConfig keyVaultConfig = new AzureKeyVaultConfig(null);

    Set<ConstraintViolation<AzureKeyVaultConfig>> violations = validator.validate(keyVaultConfig);
    assertThat(violations).hasSize(1);

    ConstraintViolation<AzureKeyVaultConfig> violation = violations.iterator().next();
    assertThat(violation.getMessageTemplate())
        .isEqualTo("{jakarta.validation.constraints.NotNull.message}");
  }

  @Test
  public void hashicorpVaultConfigWithNoUrlCreatesNotNullViolation() {
    HashicorpKeyVaultConfig keyVaultConfig = new HashicorpKeyVaultConfig();

    Set<ConstraintViolation<HashicorpKeyVaultConfig>> violations =
        validator.validate(keyVaultConfig);
    assertThat(violations).hasSize(1);

    ConstraintViolation<HashicorpKeyVaultConfig> violation = violations.iterator().next();
    assertThat(violation.getMessageTemplate())
        .isEqualTo("{jakarta.validation.constraints.NotNull.message}");
  }

  @Test
  public void serverAddressValidations() {

    String[] invalidAddresses = {"/foo/bar", "foo@bar.com,:/fff.ll", "file:/tmp/valid.somename"};

    ServerConfig config = new ServerConfig();
    for (String sample : invalidAddresses) {
      config.setServerAddress(sample);
      Set<ConstraintViolation<ServerConfig>> validresult =
          validator.validateProperty(config, "serverAddress");
      assertThat(validresult).hasSize(1);
    }

    String[] validSamples = {
      "unix:/foo/bar.ipc", "http://localhost:8080", "https://somestrangedomain.com:8080"
    };
    for (String sample : validSamples) {
      config.setServerAddress(sample);
      Set<ConstraintViolation<ServerConfig>> validresult =
          validator.validateProperty(config, "serverAddress");
      assertThat(validresult).isEmpty();
    }
  }

  @Test
  public void unknownServerType() {
    final ServerConfig serverConfig = new ServerConfig();
    serverConfig.setApp(null);

    final Config config = new Config();
    config.setServerConfigs(Collections.singletonList(serverConfig));

    final Set<ConstraintViolation<Config>> invalidresult = validator.validate(config);
    final List<ConstraintViolation<Config>> invalidServerAppTypeResults =
        invalidresult.stream()
            .filter(
                v ->
                    v.getMessageTemplate()
                        .contains(
                            "app must be provided for serverConfig and be one of P2P, Q2T, ThirdParty, ENCLAVE"))
            .collect(Collectors.toList());

    assertThat(invalidServerAppTypeResults).hasSize(1);
  }

  @Test
  public void adminServerTypeDeprecated() {
    final ServerConfig serverConfig = new ServerConfig();
    serverConfig.setApp(AppType.ADMIN);

    final Config config = new Config();
    config.setServerConfigs(Collections.singletonList(serverConfig));

    final Set<ConstraintViolation<Config>> invalidresult = validator.validate(config);
    final List<ConstraintViolation<Config>> invalidServerAppTypeResults =
        invalidresult.stream()
            .filter(
                v ->
                    v.getMessageTemplate()
                        .contains(
                            "app must be provided for serverConfig and be one of P2P, Q2T, ThirdParty, ENCLAVE"))
            .collect(Collectors.toList());

    assertThat(invalidServerAppTypeResults).hasSize(1);
  }

  @Test
  public void configHasKeysOrIsRemoteEnclaveNiether() {

    Config config = new Config();

    List<ConstraintViolation<Config>> constrainViolations =
        validator.validate(config).stream()
            .filter(v -> v.getMessageTemplate().equals("{HasKeysOrRemoteEnclave.message}"))
            .collect(Collectors.toList());

    assertThat(constrainViolations).hasSize(1);
  }

  @Test
  public void configHasKeysOrIsRemoteEnclaveRemoteEnclave() {

    Config config = new Config();
    config.setServerConfigs(new ArrayList<>());
    ServerConfig enclaveConfig =
        new ServerConfig() {
          {
            setApp(AppType.ENCLAVE);
          }
        };
    config.getServerConfigs().add(enclaveConfig);

    List<ConstraintViolation<Config>> constrainViolations =
        validator.validate(config).stream()
            .filter(v -> v.getMessageTemplate().equals("{HasKeysOrRemoteEnclave.message}"))
            .collect(Collectors.toList());

    assertThat(constrainViolations).isEmpty();
  }

  @Test
  public void configHasKeysOrIsRemoteEclaveWithKeys() {

    Config config = new Config();
    config.setKeys(new KeyConfiguration());

    List<ConstraintViolation<Config>> constrainViolations =
        validator.validate(config).stream()
            .filter(v -> v.getMessageTemplate().equals("{HasKeysOrRemoteEnclave.message}"))
            .collect(Collectors.toList());

    assertThat(constrainViolations).isEmpty();
  }

  @Test
  public void configHasKeysOrIsRemoteEnclaveNoEnclaveServerNorKeys() {

    Config config = new Config();
    config.setServerConfigs(new ArrayList<>());
    ServerConfig enclaveConfig =
        new ServerConfig() {
          {
            setApp(AppType.P2P);
          }
        };
    config.getServerConfigs().add(enclaveConfig);

    List<ConstraintViolation<Config>> constrainViolations =
        validator.validate(config).stream()
            .filter(v -> v.getMessageTemplate().equals("{HasKeysOrRemoteEnclave.message}"))
            .collect(Collectors.toList());

    assertThat(constrainViolations).hasSize(1);
  }
}
