package com.quorum.tessera.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
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
        KeyData keyData = new KeyData(keyDataConfig, "privateKey", "publicKey", null, null);
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
        KeyData keyData = new KeyData(keyDataConfig, "NACL_FAILURE", "publicKey", null, null);
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
        KeyData keyData = new KeyData(keyDataConfig, "INAVLID_BASE", "publicKey", null, null);
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

        KeyData keyData = new KeyData(null, null, null, privateKeyPath, publicKeyPath);

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

        KeyData keyData = new KeyData(null, null, null, privateKeyPath, publicKeyPath);

        KeyConfiguration keyConfiguration = new KeyConfiguration(null,null,Arrays.asList(keyData), null);

        Set<ConstraintViolation<KeyConfiguration>> violations = validator.validate(keyConfiguration);
        assertThat(violations).hasSize(1);

        ConstraintViolation<KeyConfiguration> violation = violations.iterator().next();

        assertThat(violation.getMessageTemplate()).isEqualTo("{ValidKeyData.privateKeyPath.notExists}");
        assertThat(violation.getPropertyPath().toString()).endsWith("privateKeyPath");
    }

}
