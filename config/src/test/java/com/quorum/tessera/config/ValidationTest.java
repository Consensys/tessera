package com.quorum.tessera.config;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ValidationTest {

    private final Validator validator = Validation
            .buildDefaultValidatorFactory().getValidator();

    public ValidationTest() {
    }

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
    public void keyDataConfigMissingPassword() {
        PrivateKeyData privateKeyData = new PrivateKeyData(null, "snonce", "asalt", "sbox", mock(ArgonOptions.class), null);
        KeyDataConfig keyDataConfig = new KeyDataConfig(privateKeyData, PrivateKeyType.LOCKED);
        KeyData keyData = new KeyData(keyDataConfig, "privateKey", "publicKey", null, null, null, null);
        Set<ConstraintViolation<KeyData>> violations = validator.validate(keyData);
        assertThat(violations).hasSize(1);

        ConstraintViolation<KeyData> violation = violations.iterator().next();

        assertThat(violation.getMessageTemplate()).isEqualTo("{ValidKeyDataConfig.message}");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("config");
    }

    @Test
    public void keyDataConfigNaclFailure() {
        PrivateKeyData privateKeyData = new PrivateKeyData(null, "snonce", "asalt", "sbox", mock(ArgonOptions.class), "SECRET");
        KeyDataConfig keyDataConfig = new KeyDataConfig(privateKeyData, PrivateKeyType.LOCKED);
        KeyData keyData = new KeyData(keyDataConfig, "NACL_FAILURE", "publicKey", null, null, null, null);
        Set<ConstraintViolation<KeyData>> violations = validator.validate(keyData);
        assertThat(violations).hasSize(1);

        ConstraintViolation<KeyData> violation = violations.iterator().next();

        assertThat(violation.getMessageTemplate()).isEqualTo("Could not decrypt the private key with the provided password, please double check the passwords provided");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("privateKey");
    }

    @Test
    public void keyDataConfigInvalidBase64() {
        PrivateKeyData privateKeyData = new PrivateKeyData(null, "snonce", "asalt", "sbox", mock(ArgonOptions.class), "SECRET");
        KeyDataConfig keyDataConfig = new KeyDataConfig(privateKeyData, PrivateKeyType.LOCKED);
        KeyData keyData = new KeyData(keyDataConfig, "INAVLID_BASE", "publicKey", null, null, null, null);
        Set<ConstraintViolation<KeyData>> violations = validator.validate(keyData);
        assertThat(violations).hasSize(1);

        ConstraintViolation<KeyData> violation = violations.iterator().next();

        assertThat(violation.getMessageTemplate()).isEqualTo("{ValidBase64.message}");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("privateKey");
    }

    @Test
    public void invalidAlwaysSendTo() {

        List<String> alwaysSendTo = Arrays.asList("BOGUS");

        Config config = new Config(null, null, null, null, alwaysSendTo, null, false);

        Set<ConstraintViolation<Config>> violations = validator.validateProperty(config, "alwaysSendTo");

        assertThat(violations).hasSize(1);

        ConstraintViolation<Config> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).startsWith("alwaysSendTo[0]");
        assertThat(violation.getMessageTemplate()).isEqualTo("{ValidBase64.message}");

    }

    @Test
    public void validAlwaysSendTo() {

        String value = Base64.getEncoder().encodeToString("HELLOW".getBytes());

        List<String> alwaysSendTo = Arrays.asList(value);

        Config config = new Config(null, null, null, null, alwaysSendTo, null, false);

        Set<ConstraintViolation<Config>> violations = validator.validateProperty(config, "alwaysSendTo");

        assertThat(violations).isEmpty();


    }

    @Test
    public void keyDataPublicKeyValidation() {

        Path publicKeyPath = Paths.get(UUID.randomUUID().toString());

        Path privateKeyPath = Paths.get(UUID.randomUUID().toString());

        KeyData keyData = new KeyData(null, null, null, privateKeyPath, publicKeyPath, null, null);

        KeyConfiguration keyConfiguration = new KeyConfiguration(null,null,Arrays.asList(keyData), null);

        Set<ConstraintViolation<KeyConfiguration>> violations = validator.validate(keyConfiguration);
        assertThat(violations).hasSize(1);

        ConstraintViolation<KeyConfiguration> violation = violations.iterator().next();

        assertThat(violation.getMessageTemplate()).isEqualTo("{ValidKeyData.publicKeyPath.notExists}");
        assertThat(violation.getPropertyPath().toString()).endsWith("publicKeyPath");
    }

    @Test
    public void keyDataPrivateKeyValidation() throws Exception {

        Path publicKeyPath = Files.createTempFile("keyDataPrivateKeyValidation", ".txt");
        publicKeyPath.toFile().deleteOnExit();

        Path privateKeyPath = Paths.get(UUID.randomUUID().toString());

        KeyData keyData = new KeyData(null, null, null, privateKeyPath, publicKeyPath, null, null);

        KeyConfiguration keyConfiguration = new KeyConfiguration(null,null,Arrays.asList(keyData), null);

        Set<ConstraintViolation<KeyConfiguration>> violations = validator.validate(keyConfiguration);
        assertThat(violations).hasSize(1);

        ConstraintViolation<KeyConfiguration> violation = violations.iterator().next();

        assertThat(violation.getMessageTemplate()).isEqualTo("{ValidKeyData.privateKeyPath.notExists}");
        assertThat(violation.getPropertyPath().toString()).endsWith("privateKeyPath");
    }

    @Test
    public void keyVaultIdAllowedCharacterSetIsAlphanumericAndDash() {
        String keyVaultId = "0123456789-abcdefghijklmnopqrstuvwxyz-ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        KeyData keyData = new KeyData(null, null, null, null, null, keyVaultId, keyVaultId);

        Set<ConstraintViolation<KeyData>> violations = validator.validateProperty(keyData, "azureVaultPublicKeyId");
        assertThat(violations).hasSize(0);

        violations = validator.validateProperty(keyData, "azureVaultPrivateKeyId");
        assertThat(violations).hasSize(0);
    }

    @Test
    public void keyVaultIdDisallowedCharactersCreateViolation() {
        String keyVaultId = "invalid_@!£$%^~^&_id";
        KeyData keyData = new KeyData(null, null, null, null, null, keyVaultId, keyVaultId);

        Set<ConstraintViolation<KeyData>> violations = validator.validateProperty(keyData, "azureVaultPublicKeyId");
        assertThat(violations).hasSize(1);

        ConstraintViolation<KeyData> violation = violations.iterator().next();
        assertThat(violation.getMessageTemplate()).isEqualTo("{javax.validation.constraints.Pattern.message}");

        violations = validator.validateProperty(keyData, "azureVaultPrivateKeyId");
        assertThat(violations).hasSize(1);

        violation = violations.iterator().next();
        assertThat(violation.getMessageTemplate()).isEqualTo("{javax.validation.constraints.Pattern.message}");
    }

    @Test
    public void keyVaultIdsProvidedWithoutKeyVaultConfigCreatesViolation() {
        KeyData keyData = new KeyData(null, null, "public", null, null, "privateVaultId", "publicVaultId");
        KeyConfiguration keyConfiguration = new KeyConfiguration(null, null, Arrays.asList(keyData), null);
        Config config = new Config(null, null, null, keyConfiguration, null, null, false);

        Set<ConstraintViolation<Config>> violations = validator.validateProperty(config, "keys");
        assertThat(violations).hasSize(1);

        ConstraintViolation<Config> violation = violations.iterator().next();
        assertThat(violation.getMessageTemplate()).isEqualTo("{ValidKeyVaultConfiguration.message}");
    }

    @Test
    public void keyVaultConfigProvidedWithoutKeyVaultIdsDoesNotCreateViolation() {
        KeyData keyData = new KeyData(null, null, "public", null, null, null, null);
        KeyConfiguration keyConfiguration = new KeyConfiguration(null, null, Arrays.asList(keyData), new KeyVaultConfig("url"));
        Config config = new Config(null, null, null, keyConfiguration, null, null, false);

        Set<ConstraintViolation<Config>> violations = validator.validateProperty(config, "keys");
        assertThat(violations).hasSize(0);
    }


    @Test
    public void keyConfigurationIsNullCreatesNotNullViolation() {
        Config config = new Config(null, null, null, null, null, null, false);

        Set<ConstraintViolation<Config>> violations = validator.validateProperty(config, "keys");

        assertThat(violations).hasSize(1);

        ConstraintViolation<Config> violation = violations.iterator().next();
        assertThat(violation.getMessageTemplate()).isEqualTo("{javax.validation.constraints.NotNull.message}");
    }

    @Test
    public void keyConfigurationPublicKeyOnlyCreatesViolation() {
        KeyData keyData = new KeyData(null, null, "public", null, null, null, null);
        KeyConfiguration keyConfiguration = new KeyConfiguration(null, null, Arrays.asList(keyData), null);

        Set<ConstraintViolation<KeyConfiguration>> violations = validator.validateProperty(keyConfiguration, "keyData");
        assertThat(violations).hasSize(1);

        ConstraintViolation<KeyConfiguration> violation = violations.iterator().next();
        assertThat(violation.getMessageTemplate()).isEqualTo("{ValidKeyData.bothPrivateAndPublicRequired.message}");
    }

    @Test
    public void keyConfigurationPublicKeyWithKeyVaultIdAndKeyVaultConfigCreatesViolation() {
        KeyData keyData = new KeyData(null, null, "public", null, null, "privateVaultId", null);
        KeyConfiguration keyConfiguration = new KeyConfiguration(null, null, Arrays.asList(keyData), new KeyVaultConfig("url"));
        Config config = new Config(null, null, null, keyConfiguration, null, null, false);

        Set<ConstraintViolation<Config>> violations = validator.validateProperty(config, "keys");
        assertThat(violations).hasSize(1);

        ConstraintViolation<Config> violation = violations.iterator().next();
        assertThat(violation.getMessageTemplate()).isEqualTo("{ValidKeyData.keyTypesCannotBeInterchanged.message}");
    }

    @Test
    public void keyConfigurationKeyVaultPrivateIdOnlyCreatesViolation() {
        KeyData keyData = new KeyData(null, null, null, null, null, "privateVaultId", null);

        //TODO
    }

    @Test
    public void publicAndPrivateInlineWithKeyVaultIdsButNoKeyVaultConfigDoesNotCreateViolation() {
        KeyData keyData = new KeyData(null, "private", "public", null, null, "privateVaultId", "publicVaultId");
        KeyConfiguration keyConfiguration = new KeyConfiguration(null, null, Arrays.asList(keyData),null);
        Config config = new Config(null, null, null, keyConfiguration, null, null, false);

        Set<ConstraintViolation<Config>> violations = validator.validateProperty(config, "keys");
        assertThat(violations).hasSize(0);
    }

    @Test
    public void keyVaultConfigWithNoUrlCreatesNullViolation() {
        KeyVaultConfig keyVaultConfig = new KeyVaultConfig(null);

        Set<ConstraintViolation<KeyVaultConfig>> violations = validator.validate(keyVaultConfig);
        assertThat(violations).hasSize(1);

        ConstraintViolation<KeyVaultConfig> violation = violations.iterator().next();
        assertThat(violation.getMessageTemplate()).isEqualTo("{javax.validation.constraints.NotNull.message}");
    }

    @Test
    public void vaultIdsProvidedButKeyVaultConfigHasNullUrlCreatesNullViolation() {
        KeyData keyData = new KeyData(null, null, null, null, null, "privateVaultId", "publicVaultId");
        KeyVaultConfig keyVaultConfig = new KeyVaultConfig(null);
        KeyConfiguration keyConfiguration = new KeyConfiguration(null, null, Arrays.asList(keyData), keyVaultConfig);

        Set<ConstraintViolation<KeyConfiguration>> violations = validator.validate(keyConfiguration);
        assertThat(violations).hasSize(1);

        ConstraintViolation<KeyConfiguration> violation = violations.iterator().next();
        assertThat(violation.getMessageTemplate()).isEqualTo("{javax.validation.constraints.NotNull.message}");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("azureKeyVaultConfig.url");
    }
}
