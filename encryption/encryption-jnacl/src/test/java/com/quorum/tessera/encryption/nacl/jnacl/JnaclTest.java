package com.quorum.tessera.encryption.nacl.jnacl;

import static com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305.crypto_secretbox_BEFORENMBYTES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.quorum.tessera.encryption.*;
import java.security.SecureRandom;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JnaclTest {

  private PublicKey publicKey;

  private PrivateKey privateKey;

  private SharedKey sharedKey;

  private byte[] message = "TEST_MESSAGE".getBytes(UTF_8);

  private Nonce nonce = new Nonce("TEST_NONCE".getBytes(UTF_8));

  private SecureRandom secureRandom;

  private SecretBox secretBox;

  private Jnacl jnacl;

  @Before
  public void init() {
    this.secureRandom = new SecureRandom();
    this.secretBox = mock(SecretBox.class);

    this.jnacl = new Jnacl(this.secureRandom, this.secretBox);

    final Jnacl setupJnacl = new Jnacl(new SecureRandom(), new JnaclSecretBox());
    final KeyPair keys = setupJnacl.generateNewKeys();
    this.publicKey = keys.getPublicKey();
    this.privateKey = keys.getPrivateKey();
    this.sharedKey = setupJnacl.computeSharedKey(publicKey, privateKey);

    this.nonce = setupJnacl.randomNonce();
  }

  @After
  public void after() {
    verifyNoMoreInteractions(this.secretBox);
  }

  @Test
  public void computingSharedKeyThrowsExceptionOnFailure() {
    doReturn(-1)
        .when(this.secretBox)
        .cryptoBoxBeforenm(
            any(byte[].class), eq(publicKey.getKeyBytes()), eq(privateKey.getKeyBytes()));

    final Throwable kaclEx =
        catchThrowable(() -> this.jnacl.computeSharedKey(publicKey, privateKey));

    assertThat(kaclEx)
        .isInstanceOf(EncryptorException.class)
        .hasMessage("jnacl could not compute the shared key");

    verify(this.secretBox)
        .cryptoBoxBeforenm(
            any(byte[].class), eq(publicKey.getKeyBytes()), eq(privateKey.getKeyBytes()));
  }

  @Test
  public void sealUsingKeysThrowsExceptionOnFailure() {

    final Throwable kaclEx =
        catchThrowable(
            () -> this.jnacl.seal(message, nonce, publicKey, PrivateKey.from(new byte[] {})));

    assertThat(kaclEx).isInstanceOf(EncryptorException.class).hasMessage("Private key too short");
  }

  @Test
  public void openUsingKeysThrowsExceptionOnFailure() {
    final Throwable kaclEx =
        catchThrowable(
            () -> this.jnacl.open(message, nonce, publicKey, PrivateKey.from(new byte[] {})));

    assertThat(kaclEx).isInstanceOf(EncryptorException.class).hasMessage("Private key too short");
  }

  @Test
  public void sealUsingSharedkeyThrowsExceptionOnFailure() {
    doReturn(-1)
        .when(this.secretBox)
        .cryptoBoxAfternm(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            eq(sharedKey.getKeyBytes()));

    final Throwable kaclEx =
        catchThrowable(() -> this.jnacl.sealAfterPrecomputation(message, nonce, sharedKey));

    assertThat(kaclEx)
        .isInstanceOf(EncryptorException.class)
        .hasMessage("jnacl could not seal the payload using the shared key");

    verify(this.secretBox)
        .cryptoBoxAfternm(
            any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class));
  }

  @Test
  public void openUsingSharedkeyThrowsExceptionOnFailure() {
    doReturn(-1)
        .when(this.secretBox)
        .cryptoBoxOpenAfternm(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            eq(sharedKey.getKeyBytes()));

    final Throwable kaclEx =
        catchThrowable(() -> this.jnacl.openAfterPrecomputation(message, nonce, sharedKey));

    assertThat(kaclEx)
        .isInstanceOf(EncryptorException.class)
        .hasMessage("jnacl could not open the payload using the shared key");

    verify(this.secretBox)
        .cryptoBoxOpenAfternm(
            any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class));
  }

  @Test
  public void nonceContainsRandomData() {
    final Nonce nonce = this.jnacl.randomNonce();

    assertThat(nonce.getNonceBytes()).hasSize(24);
  }

  @Test
  public void generatingNewKeysThrowsExceptionOnFailure() {
    doReturn(-1).when(this.secretBox).cryptoBoxKeypair(any(byte[].class), any(byte[].class));

    final Throwable kaclEx = catchThrowable(() -> this.jnacl.generateNewKeys());

    assertThat(kaclEx)
        .isInstanceOf(EncryptorException.class)
        .hasMessage("jnacl could not generate a new public/private keypair");

    verify(this.secretBox).cryptoBoxKeypair(any(byte[].class), any(byte[].class));
  }

  @Test
  public void computeSharedKeySodiumReturnsSuccess() {

    doReturn(1)
        .when(this.secretBox)
        .cryptoBoxBeforenm(any(byte[].class), any(byte[].class), any(byte[].class));

    final SharedKey result = this.jnacl.computeSharedKey(publicKey, privateKey);

    assertThat(result).isNotNull();

    assertThat(result.getKeyBytes()).isEqualTo(new byte[crypto_secretbox_BEFORENMBYTES]);

    verify(this.secretBox)
        .cryptoBoxBeforenm(any(byte[].class), any(byte[].class), any(byte[].class));
  }

  @Test
  public void generateNewKeysSodiumSuccess() {

    final KeyPair result = this.jnacl.generateNewKeys();

    assertThat(result).isNotNull();

    assertThat(result.getPrivateKey()).isNotNull();
    assertThat(result.getPublicKey()).isNotNull();

    assertThat(result.getPublicKey().getKeyBytes()).hasSize(32);
    assertThat(result.getPrivateKey().getKeyBytes()).hasSize(32);

    verify(this.secretBox).cryptoBoxKeypair(any(byte[].class), any(byte[].class));
  }

  @Test
  public void sealAfterPrecomputationSodiumReturnsSuccess() {

    doReturn(1)
        .when(this.secretBox)
        .cryptoBoxAfternm(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            eq(sharedKey.getKeyBytes()));

    final byte[] result = this.jnacl.sealAfterPrecomputation(message, nonce, sharedKey);

    assertThat(result).isNotEmpty();

    verify(this.secretBox)
        .cryptoBoxAfternm(
            any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class));
  }

  @Test
  public void openUsingSharedKeySodiumReturnsSuccess() {

    final byte[] data = new byte[100];

    doReturn(1)
        .when(this.secretBox)
        .cryptoBoxOpenAfternm(
            any(byte[].class), eq(data), anyInt(), any(byte[].class), eq(sharedKey.getKeyBytes()));

    final byte[] results = this.jnacl.openAfterPrecomputation(data, nonce, sharedKey);

    assertThat(results).isNotEmpty();

    verify(this.secretBox)
        .cryptoBoxOpenAfternm(
            any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class));
  }

  @Test
  public void openUsingKeysSodiumReturnsSucesss() {

    final byte[] data = new byte[100];

    final byte[] result = this.jnacl.open(data, nonce, publicKey, privateKey);

    assertThat(result).isNotEmpty();
  }

  @Test
  public void sealUsingKeysSodiumReturnsSuccess() {

    final byte[] results = this.jnacl.seal(message, nonce, publicKey, privateKey);

    assertThat(results).isNotEmpty();
  }

  @Test
  public void generatingRandomKeyReturnsCorrectSize() {
    final int expectedKeysize = 32;

    final SharedKey key = this.jnacl.createSingleKey();

    assertThat(key.getKeyBytes()).hasSize(expectedKeysize);
  }
}
