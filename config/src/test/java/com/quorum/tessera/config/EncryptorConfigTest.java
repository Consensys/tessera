package com.quorum.tessera.config;

import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class EncryptorConfigTest {

    @Test
    public void twoOfSameTypeAndEmptyProperteisAreEqual() {

        EncryptorConfig encryptorConfig = new EncryptorConfig();
        encryptorConfig.setType(EncryptorType.NACL);

        EncryptorConfig otherEncryptorConfig = new EncryptorConfig();
        otherEncryptorConfig.setType(EncryptorType.NACL);

        assertThat(encryptorConfig).isEqualTo(otherEncryptorConfig);
        assertThat(encryptorConfig).isEqualTo(encryptorConfig);
        assertThat(encryptorConfig).isNotEqualTo(new HashMap());

    }

    @Test
    public void notEqualsNull() {

        EncryptorConfig encryptorConfig = new EncryptorConfig();
        encryptorConfig.setType(EncryptorType.NACL);

        assertThat(encryptorConfig).isNotEqualTo(null);

    }

    @Test
    public void differentTypesNotEqual() {

        EncryptorConfig encryptorConfig = new EncryptorConfig();
        encryptorConfig.setType(EncryptorType.NACL);

        EncryptorConfig otherEncryptorConfig = new EncryptorConfig();
        otherEncryptorConfig.setType(EncryptorType.EC);

        assertThat(encryptorConfig).isNotEqualTo(otherEncryptorConfig);

    }

    @Test
    public void twoOfSameTypeAndDifferntPropertiesAreNotEqual() {

        EncryptorConfig encryptorConfig = new EncryptorConfig();
        encryptorConfig.setType(EncryptorType.NACL);

        EncryptorConfig otherEncryptorConfig = new EncryptorConfig();
        otherEncryptorConfig.setType(EncryptorType.NACL);
        
        Map<String, String> props = new HashMap<>();
        props.put("foo", "bar");

        otherEncryptorConfig.setProperties(props);

        assertThat(encryptorConfig).isNotEqualTo(otherEncryptorConfig);

    }

}
