package com.quorum.tessera.config.keypairs;

import static com.quorum.tessera.config.PrivateKeyType.UNLOCKED;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.constraints.ValidBase64;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.EncryptorException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Objects;

public class InlineKeypair implements ConfigKeyPair {

  @XmlElement private final String publicKey;

  @NotNull
  @XmlElement(name = "config")
  private final KeyDataConfig privateKeyConfig;

  private char[] password;

  private String cachedValue;

  private char[] cachedPassword;

  @XmlTransient private KeyEncryptor keyEncryptor;

  public InlineKeypair(
      final String publicKey, final KeyDataConfig privateKeyConfig, KeyEncryptor keyEncryptor) {
    this.publicKey = publicKey;
    this.privateKeyConfig = privateKeyConfig;
    this.keyEncryptor = keyEncryptor;
  }

  public KeyDataConfig getPrivateKeyConfig() {
    return this.privateKeyConfig;
  }

  @Override
  @Size(min = 1)
  @NotNull
  @ValidBase64(message = "Invalid Base64 key provided")
  public String getPublicKey() {
    return this.publicKey;
  }

  @Override
  @NotNull
  @Size(min = 1)
  @ValidBase64(message = "Invalid Base64 key provided")
  @Pattern(
      regexp = "^((?!NACL_FAILURE).)*$",
      message =
          "Could not decrypt the private key with the provided password, please double check the passwords provided")
  public String getPrivateKey() {
    final PrivateKeyData pkd = privateKeyConfig.getPrivateKeyData();

    if (privateKeyConfig.getType() == UNLOCKED) {
      return privateKeyConfig.getValue();
    }

    if (this.cachedValue == null || !Objects.equals(this.cachedPassword, this.password)) {
      if (password != null) {
        try {
          this.cachedValue = keyEncryptor.decryptPrivateKey(pkd, password).encodeToBase64();
        } catch (final EncryptorException ex) {
          this.cachedValue = "NACL_FAILURE";
        }
      }
    }

    this.cachedPassword = this.password;

    return this.cachedValue;
  }

  @Override
  public void withPassword(final char[] password) {
    this.password = password;
  }

  @Override
  public char[] getPassword() {
    return this.password;
  }
}
