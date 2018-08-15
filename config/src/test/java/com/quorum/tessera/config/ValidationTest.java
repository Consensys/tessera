package com.quorum.tessera.config;

import java.util.Set;
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
}
