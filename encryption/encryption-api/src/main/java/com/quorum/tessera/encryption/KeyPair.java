package com.quorum.tessera.encryption;

import java.util.Objects;

/**
 * Container object for a public/private key pair
 *
 * <p>The public and private key should be related to each other, not just an arbitrary pairing of
 * two keys
 */
public class KeyPair {

  private final PublicKey publicKey;

  private final PrivateKey privateKey;

  public KeyPair(final PublicKey publicKey, final PrivateKey privateKey) {
    this.publicKey = Objects.requireNonNull(publicKey);
    this.privateKey = Objects.requireNonNull(privateKey);
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  @Override
  public boolean equals(final Object o) {
    return (o instanceof KeyPair)
        && Objects.equals(publicKey, ((KeyPair) o).publicKey)
        && Objects.equals(privateKey, ((KeyPair) o).privateKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPublicKey(), getPrivateKey());
  }
}
