package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.constraints.ValidBase64;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;

public class DirectKeyPair implements ConfigKeyPair {

  @Size(min = 1)
  @NotNull
  @ValidBase64(message = "Invalid Base64 key provided")
  @XmlElement
  private final String publicKey;

  @Size(min = 1)
  @NotNull
  @ValidBase64(message = "Invalid Base64 key provided")
  @XmlElement
  private final String privateKey;

  public DirectKeyPair(final String publicKey, final String privateKey) {
    this.publicKey = publicKey;
    this.privateKey = privateKey;
  }

  @Override
  public String getPublicKey() {
    return this.publicKey;
  }

  @Override
  public String getPrivateKey() {
    return this.privateKey;
  }

  @Override
  public void withPassword(final char[] password) {
    // no need to keep a password for this key type
  }

  @Override
  public char[] getPassword() {
    // no password to return
    return new char[0];
  }
}
