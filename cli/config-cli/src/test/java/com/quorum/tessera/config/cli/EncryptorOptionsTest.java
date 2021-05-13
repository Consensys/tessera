package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import org.junit.Test;
import picocli.CommandLine;

public class EncryptorOptionsTest {

  @Test
  public void ellipticalCurveNoPropertiesDefined() {
    EncryptorOptions encryptorOptions = new EncryptorOptions();
    String[] args = new String[] {"--encryptor.type=EC"};

    new CommandLine(encryptorOptions).parseArgs(args);

    EncryptorConfig result = encryptorOptions.parseEncryptorConfig();

    assertThat(result).isNotNull();
    assertThat(result.getType()).isEqualTo(EncryptorType.EC);
    assertThat(result.getProperties()).isEmpty();
  }

  @Test
  public void ellipticalCurveWithDefinedProperties() {
    EncryptorOptions encryptorOptions = new EncryptorOptions();

    String[] args =
        new String[] {
          "--encryptor.type=EC",
          "--encryptor.symmetricCipher=somecipher",
          "--encryptor.ellipticCurve=somecurve",
          "--encryptor.nonceLength=3",
          "--encryptor.sharedKeyLength=2"
        };

    new CommandLine(encryptorOptions).parseArgs(args);

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
    String[] args = new String[] {"--encryptor.type=CUSTOM"};
    new CommandLine(encryptorOptions).parseArgs(args);
    EncryptorConfig result = encryptorOptions.parseEncryptorConfig();

    assertThat(result).isNotNull();
    assertThat(result.getType()).isEqualTo(EncryptorType.CUSTOM);
    assertThat(result.getProperties()).isEmpty();
  }
}
