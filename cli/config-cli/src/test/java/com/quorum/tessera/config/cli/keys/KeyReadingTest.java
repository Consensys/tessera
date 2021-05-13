package com.quorum.tessera.config.cli.keys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.cli.keypassresolver.CliKeyPasswordResolver;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.config.util.KeyDataUtil;
import com.quorum.tessera.passwords.PasswordReader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class KeyReadingTest {

  private PasswordReader passwordReader;

  private CliKeyPasswordResolver adapter;

  @Before
  public void init() {
    this.passwordReader = mock(PasswordReader.class);

    this.adapter = new CliKeyPasswordResolver(passwordReader);
  }

  @Test
  public void publicPrivateInlineUnlocked() {
    final Config config =
        JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/keytests/pubPrivInlineUnlocked.json"), Config.class);
    adapter.resolveKeyPasswords(config);

    assertThat(config).isNotNull();
    assertThat(config.getKeys()).isNotNull();
    assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
    assertThat(config.getKeys().getKeyData().get(0).getPublicKey())
        .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    assertThat(config.getKeys().getKeyData().get(0).getPrivateKey())
        .isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");
  }

  @Test
  public void publicPrivateInlineLocked() {
    final Config config =
        JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/keytests/pubPrivInlineLocked.json"), Config.class);
    adapter.resolveKeyPasswords(config);

    assertThat(config).isNotNull();
    assertThat(config.getKeys()).isNotNull();
    assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
    assertThat(config.getKeys().getKeyData().get(0).getPublicKey())
        .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    assertThat(config.getKeys().getKeyData().get(0).getPrivateKey())
        .isEqualTo("gZ+NvhPTi3MDaGNVvQLtlT83oEtsr2DlXww3zXnJ7mU=");
  }

  @Test
  public void passwordsInFile() {
    final Config config =
        JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/keytests/pubPrivPasswordsFile.json"), Config.class);
    adapter.resolveKeyPasswords(config);

    assertThat(config).isNotNull();
    assertThat(config.getKeys()).isNotNull();
    assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);

    KeyEncryptor keyEncryptor = KeyEncryptorFactory.newFactory().create(config.getEncryptor());
    ConfigKeyPair keyPair =
        KeyDataUtil.unmarshal(config.getKeys().getKeyData().get(0), keyEncryptor);
    assertThat(keyPair.getPublicKey()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    assertThat(keyPair.getPrivateKey()).isEqualTo("gZ+NvhPTi3MDaGNVvQLtlT83oEtsr2DlXww3zXnJ7mU=");
  }

  @Test
  public void pubPrivUsingPassLocked() {
    final Config config =
        JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/keytests/pubPrivUsingPathsLocked.json"), Config.class);
    adapter.resolveKeyPasswords(config);

    assertThat(config).isNotNull();
    assertThat(config.getKeys()).isNotNull();
    assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
    assertThat(config.getKeys().getKeyData().get(0).getPublicKey())
        .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    assertThat(config.getKeys().getKeyData().get(0).getPrivateKey())
        .isEqualTo("gZ+NvhPTi3MDaGNVvQLtlT83oEtsr2DlXww3zXnJ7mU=");
  }

  @Test
  public void pubPrivUsingPassUnlocked() {
    final Config config =
        JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/keytests/pubPrivUsingPathsUnlocked.json"),
            Config.class);
    adapter.resolveKeyPasswords(config);

    assertThat(config).isNotNull();
    assertThat(config.getKeys()).isNotNull();
    assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
    assertThat(config.getKeys().getKeyData().get(0).getPublicKey())
        .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    assertThat(config.getKeys().getKeyData().get(0).getPrivateKey())
        .isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");
  }

  @Test
  public void wrongPasswordsProvided() {
    when(passwordReader.readPasswordFromConsole()).thenReturn("invalid".toCharArray());

    final Config config =
        JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/keytests/passwordsWrong.json"), Config.class);
    adapter.resolveKeyPasswords(config);

    // a null response indicates an error occurred
    assertThat(config.getKeys().getKeyData()).hasSize(1);
    assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).startsWith("NACL_FAILURE");
  }
}
