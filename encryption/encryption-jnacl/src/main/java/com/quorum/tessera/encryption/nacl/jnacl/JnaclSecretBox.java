package com.quorum.tessera.encryption.nacl.jnacl;

import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;

/** An implementation of {@link SecretBox} that delegates to the JNaCL static methods */
public class JnaclSecretBox implements SecretBox {

  @Override
  public int cryptoBoxBeforenm(
      final byte[] output, final byte[] publicKey, final byte[] privateKey) {
    return curve25519xsalsa20poly1305.crypto_box_beforenm(output, publicKey, privateKey);
  }

  @Override
  public int cryptoBoxAfternm(
      final byte[] output,
      final byte[] message,
      final int messageLength,
      final byte[] nonce,
      final byte[] sharedKey) {
    return curve25519xsalsa20poly1305.crypto_box_afternm(
        output, message, messageLength, nonce, sharedKey);
  }

  @Override
  public int cryptoBoxOpenAfternm(
      final byte[] output,
      final byte[] message,
      final int messageLength,
      final byte[] nonce,
      final byte[] sharedKey) {
    return curve25519xsalsa20poly1305.crypto_box_open_afternm(
        output, message, messageLength, nonce, sharedKey);
  }

  @Override
  public int cryptoBoxKeypair(final byte[] publicKey, final byte[] privateKey) {
    return curve25519xsalsa20poly1305.crypto_box_keypair(publicKey, privateKey);
  }
}
