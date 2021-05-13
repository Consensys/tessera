package com.quorum.tessera.encryption;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyManagerImpl implements KeyManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyManagerImpl.class);

  /** A list of all pub/priv keys that are attached to this node */
  private final Set<KeyPair> localKeys;

  private final KeyPair defaultKeys;

  private final Set<PublicKey> forwardingPublicKeys;

  public KeyManagerImpl(final Collection<KeyPair> keys, Collection<PublicKey> forwardKeys) {

    this.localKeys = new HashSet<>(keys);

    this.defaultKeys = localKeys.iterator().next();

    this.forwardingPublicKeys = new HashSet<>(forwardKeys);
  }

  @Override
  public PublicKey getPublicKeyForPrivateKey(final PrivateKey privateKey) {
    LOGGER.debug("Attempting to find public key for the private key {}", privateKey);

    final PublicKey publicKey =
        localKeys.stream()
            .filter(keypair -> Objects.equals(keypair.getPrivateKey(), privateKey))
            .findFirst()
            .map(KeyPair::getPublicKey)
            .orElseThrow(
                () ->
                    new KeyNotFoundException(
                        "Private key "
                            + privateKey.encodeToBase64()
                            + " not found when searching for public key"));

    LOGGER.debug("Found public key {} for private key {}", publicKey, privateKey);

    return publicKey;
  }

  @Override
  public PrivateKey getPrivateKeyForPublicKey(final PublicKey publicKey) {
    LOGGER.debug("Attempting to find private key for the public key {}", publicKey);

    final PrivateKey privateKey =
        localKeys.stream()
            .filter(keypair -> Objects.equals(keypair.getPublicKey(), publicKey))
            .findFirst()
            .map(KeyPair::getPrivateKey)
            .orElseThrow(
                () ->
                    new KeyNotFoundException(
                        "Public key "
                            + publicKey.encodeToBase64()
                            + " not found when searching for private key"));

    LOGGER.debug("Found private key {} for public key {}", privateKey, publicKey);

    return privateKey;
  }

  @Override
  public Set<PublicKey> getPublicKeys() {
    return localKeys.stream().map(KeyPair::getPublicKey).collect(Collectors.toSet());
  }

  @Override
  public PublicKey defaultPublicKey() {
    return defaultKeys.getPublicKey();
  }

  @Override
  public Set<PublicKey> getForwardingKeys() {
    return this.forwardingPublicKeys;
  }
}
