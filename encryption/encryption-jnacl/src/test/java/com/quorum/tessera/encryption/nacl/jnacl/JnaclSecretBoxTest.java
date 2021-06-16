package com.quorum.tessera.encryption.nacl.jnacl;

import static com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class JnaclSecretBoxTest {

  private JnaclSecretBox secretBox = new JnaclSecretBox();

  private byte[] publicKey;

  private byte[] privateKey;

  @Before
  public void init() {
    this.publicKey = new byte[crypto_secretbox_PUBLICKEYBYTES];
    this.privateKey = new byte[crypto_secretbox_SECRETKEYBYTES];

    this.secretBox.cryptoBoxKeypair(publicKey, privateKey);
  }

  @Test
  public void sharedKeyUsingValidKeys() {
    final byte[] sharedKey = new byte[crypto_secretbox_BEFORENMBYTES];

    final int success = this.secretBox.cryptoBoxBeforenm(sharedKey, publicKey, privateKey);

    assertThat(success).isEqualTo(0);
  }

  @Test
  public void sharedKeyFailsIfOutputTooSmall() {
    final byte[] sharedKey = new byte[crypto_secretbox_BEFORENMBYTES - 1];

    final Throwable throwable =
        catchThrowable(() -> this.secretBox.cryptoBoxBeforenm(sharedKey, publicKey, privateKey));

    assertThat(throwable).isInstanceOf(ArrayIndexOutOfBoundsException.class);
  }

  @Test
  public void sharedKeyFailsIfPublicKeyTooSmall() {
    final byte[] sharedKey = new byte[crypto_secretbox_BEFORENMBYTES];

    final Throwable throwable =
        catchThrowable(
            () ->
                this.secretBox.cryptoBoxBeforenm(
                    sharedKey, Arrays.copyOf(publicKey, publicKey.length - 1), privateKey));

    assertThat(throwable).isInstanceOf(ArrayIndexOutOfBoundsException.class);
  }

  @Test
  public void sharedKeyFailsIfPrivateKeyTooSmall() {
    final byte[] sharedKey = new byte[crypto_secretbox_BEFORENMBYTES];

    final Throwable throwable =
        catchThrowable(
            () ->
                this.secretBox.cryptoBoxBeforenm(
                    sharedKey, publicKey, Arrays.copyOf(privateKey, privateKey.length - 1)));

    assertThat(throwable).isInstanceOf(ArrayIndexOutOfBoundsException.class);
  }

  @Test
  public void sealingMessageUsingSymmetricKeyFailsIfMessageIsLessThanRequiredLength() {

    final int success =
        this.secretBox.cryptoBoxAfternm(new byte[0], new byte[0], 0, new byte[0], new byte[0]);

    assertThat(success).isEqualTo(-1);
  }

  @Test
  public void sealingMessageUsingSymmetricKeySucceeds() {
    final int success =
        this.secretBox.cryptoBoxAfternm(new byte[32], new byte[32], 32, new byte[24], new byte[32]);

    assertThat(success).isEqualTo(0);
  }

  @Test
  public void generatingNewKeysSucceedsIfArraysAreBigEnough() {
    final byte[] publicKey = new byte[crypto_secretbox_PUBLICKEYBYTES];
    final byte[] privateKey = new byte[crypto_secretbox_SECRETKEYBYTES];

    final int success = this.secretBox.cryptoBoxKeypair(publicKey, privateKey);

    assertThat(success).isEqualTo(0);
  }

  @Test
  public void generatingNewsKeysFailsIfPublicKeyTooSmall() {

    final byte[] publicKey = new byte[crypto_secretbox_PUBLICKEYBYTES - 1];
    final byte[] privateKey = new byte[crypto_secretbox_SECRETKEYBYTES];

    final Throwable throwable =
        catchThrowable(() -> this.secretBox.cryptoBoxKeypair(publicKey, privateKey));

    assertThat(throwable).isInstanceOf(ArrayIndexOutOfBoundsException.class);
  }

  @Test
  public void generatingNewsKeysFailsIfPrivateKeyTooSmall() {

    final byte[] publicKey = new byte[crypto_secretbox_PUBLICKEYBYTES];
    final byte[] privateKey = new byte[crypto_secretbox_SECRETKEYBYTES - 1];

    final Throwable throwable =
        catchThrowable(() -> this.secretBox.cryptoBoxKeypair(publicKey, privateKey));

    assertThat(throwable).isInstanceOf(ArrayIndexOutOfBoundsException.class);
  }

  @Test
  public void openingBoxFailsIfInputLengthTooSmall() {

    final int success =
        this.secretBox.cryptoBoxOpenAfternm(new byte[0], new byte[0], 0, new byte[0], new byte[0]);

    assertThat(success).isEqualTo(-1);
  }

  @Test
  public void openingFailsIfInputsAreInvalid() {

    final int success =
        this.secretBox.cryptoBoxOpenAfternm(
            new byte[32], new byte[32], 32, new byte[24], new byte[32]);

    assertThat(success).isEqualTo(-1);
  }

  @Test
  public void openingSucceedsIfInputsAreValid() {

    // setup
    final byte[] sharedKey = new byte[crypto_secretbox_BEFORENMBYTES];
    this.secretBox.cryptoBoxBeforenm(sharedKey, publicKey, privateKey);

    final byte[] nonce = new byte[crypto_secretbox_NONCEBYTES];
    final byte[] message = new byte[33];
    message[32] = 1;
    final byte[] cipherText = new byte[33];
    this.secretBox.cryptoBoxAfternm(cipherText, message, 33, nonce, sharedKey);

    // make the call
    final int success =
        this.secretBox.cryptoBoxOpenAfternm(new byte[33], cipherText, 33, nonce, sharedKey);

    // check result
    assertThat(success).isEqualTo(0);
  }
}
