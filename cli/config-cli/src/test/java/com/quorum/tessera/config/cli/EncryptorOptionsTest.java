package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptorOptionsTest {

    @Test
    public void ellipticalCurveNoPropertiesDefined() {
        EncryptorOptions encryptorOptions = new EncryptorOptions();
        encryptorOptions.type = EncryptorType.EC;

        EncryptorConfig result = encryptorOptions.parseEncryptorConfig();

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(EncryptorType.EC);
        assertThat(result.getProperties()).isEmpty();
    }

    @Test
    public void ellipticalCurveWithDefinedProperties() {
        EncryptorOptions encryptorOptions = new EncryptorOptions();
        encryptorOptions.type = EncryptorType.EC;
        encryptorOptions.symmetricCipher = "somecipher";
        encryptorOptions.ellipticCurve = "somecurve";
        encryptorOptions.nonceLength = "3";
        encryptorOptions.sharedKeyLength = "2";

        EncryptorConfig result = encryptorOptions.parseEncryptorConfig();

        assertThat(result.getType()).isEqualTo(EncryptorType.EC);
        assertThat(result.getProperties())
                .containsOnlyKeys("symmetricCipher", "ellipticCurve", "nonceLength", "sharedKeyLength");

        assertThat(result.getProperties().get("symmetricCipher")).isEqualTo("somecipher");
        assertThat(result.getProperties().get("ellipticCurve")).isEqualTo("somecurve");
        assertThat(result.getProperties().get("nonceLength")).isEqualTo("3");
        assertThat(result.getProperties().get("sharedKeyLength")).isEqualTo("2");
    }

    @Test
    public void encryptorTypeDefaultsToNACL() {
        EncryptorOptions encryptorOptions = new EncryptorOptions();

        EncryptorConfig result = encryptorOptions.parseEncryptorConfig();

        assertThat(result.getType()).isEqualTo(EncryptorType.NACL);
        assertThat(result.getProperties()).isEmpty();
    }

    @Test
    public void encryptorTypeCUSTOM() {
        EncryptorOptions encryptorOptions = new EncryptorOptions();
        encryptorOptions.type = EncryptorType.CUSTOM;

        EncryptorConfig result = encryptorOptions.parseEncryptorConfig();

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(EncryptorType.CUSTOM);
        assertThat(result.getProperties()).isEmpty();
    }
}
