package com.quorum.tessera.encryption.nacl.kalium;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.abstractj.kalium.NaCl.Sodium.CRYPTO_BOX_CURVE25519XSALSA20POLY1305_BEFORENMBYTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.encryption.SharedKey;
import org.abstractj.kalium.NaCl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KaliumTest {

  private PublicKey publicKey = PublicKey.from("publickey".getBytes(UTF_8));

  private PrivateKey privateKey = PrivateKey.from("privateKey".getBytes(UTF_8));

  private SharedKey sharedKey = SharedKey.from("sharedKey".getBytes(UTF_8));

  private byte[] message = "TEST_MESSAGE".getBytes(UTF_8);

  private Nonce nonce = new Nonce("TEST_NONCE".getBytes(UTF_8));

  private NaCl.Sodium sodium;

  private Kalium kalium;

  @Before
  public void init() {
    this.sodium = mock(NaCl.Sodium.class);

    this.kalium = new Kalium(this.sodium);
  }

  @After
  public void after() {
    verify(this.sodium).sodium_init();
    verifyNoMoreInteractions(this.sodium);
  }

  @Test
  public void sodiumIsInitialisedOnStartup() {
    final NaCl.Sodium sodium = mock(NaCl.Sodium.class);

    // FIXME Is this being used?
    final Kalium kalium = new Kalium(sodium);

    verify(sodium).sodium_init();
  }

  @Test
  public void computingSharedKeyThrowsExceptionOnFailure() {
    doReturn(-1)
        .when(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_beforenm(
            any(byte[].class), eq(publicKey.getKeyBytes()), eq(privateKey.getKeyBytes()));

    final Throwable kaclEx =
        catchThrowable(() -> this.kalium.computeSharedKey(publicKey, privateKey));

    assertThat(kaclEx)
        .isInstanceOf(EncryptorException.class)
        .hasMessage("Kalium could not compute the shared key");

    verify(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_beforenm(
            any(byte[].class), eq(publicKey.getKeyBytes()), eq(privateKey.getKeyBytes()));
  }

  @Test
  public void sealUsingKeysThrowsExceptionOnFailure() {
    doReturn(-1)
        .when(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            eq(publicKey.getKeyBytes()),
            eq(privateKey.getKeyBytes()));

    final Throwable kaclEx =
        catchThrowable(() -> this.kalium.seal(message, nonce, publicKey, privateKey));

    assertThat(kaclEx)
        .isInstanceOf(EncryptorException.class)
        .hasMessage("Kalium could not seal the payload using the provided keys directly");

    verify(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            any(byte[].class),
            any(byte[].class));
  }

  @Test
  public void openUsingKeysThrowsExceptionOnFailure() {
    doReturn(-1)
        .when(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_open(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            eq(publicKey.getKeyBytes()),
            eq(privateKey.getKeyBytes()));

    final Throwable kaclEx =
        catchThrowable(() -> this.kalium.open(message, nonce, publicKey, privateKey));

    assertThat(kaclEx)
        .isInstanceOf(EncryptorException.class)
        .hasMessage("Kalium could not open the payload using the provided keys directly");

    verify(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_open(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            any(byte[].class),
            any(byte[].class));
  }

  @Test
  public void sealUsingSharedkeyThrowsExceptionOnFailure() {
    doReturn(-1)
        .when(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_afternm(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            eq(sharedKey.getKeyBytes()));

    final Throwable kaclEx =
        catchThrowable(() -> this.kalium.sealAfterPrecomputation(message, nonce, sharedKey));

    assertThat(kaclEx)
        .isInstanceOf(EncryptorException.class)
        .hasMessage("Kalium could not seal the payload using the shared key");

    verify(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_afternm(
            any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class));
  }

  @Test
  public void openUsingSharedkeyThrowsExceptionOnFailure() {
    doReturn(-1)
        .when(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_open_afternm(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            eq(sharedKey.getKeyBytes()));

    final Throwable kaclEx =
        catchThrowable(() -> this.kalium.openAfterPrecomputation(message, nonce, sharedKey));

    assertThat(kaclEx)
        .isInstanceOf(EncryptorException.class)
        .hasMessage("Kalium could not open the payload using the shared key");

    verify(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_open_afternm(
            any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class));
  }

  @Test
  public void nonceContainsRandomData() {
    this.kalium.randomNonce();

    verify(this.sodium).randombytes(any(byte[].class), anyInt());
  }

  @Test
  public void generatingNewKeysThrowsExceptionOnFailure() {
    doReturn(-1)
        .when(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_keypair(any(byte[].class), any(byte[].class));

    final Throwable kaclEx = catchThrowable(() -> this.kalium.generateNewKeys());

    assertThat(kaclEx)
        .isInstanceOf(EncryptorException.class)
        .hasMessage("Kalium could not generate a new public/private keypair");

    verify(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_keypair(any(byte[].class), any(byte[].class));
  }

  @Test
  public void computeSharedKeySodiumReturnsSuccess() {

    when(sodium.crypto_box_curve25519xsalsa20poly1305_beforenm(
            any(byte[].class), any(byte[].class), any(byte[].class)))
        .thenReturn(1);

    final SharedKey result = kalium.computeSharedKey(publicKey, privateKey);

    assertThat(result).isNotNull();
    assertThat(result.getKeyBytes())
        .isEqualTo(new byte[CRYPTO_BOX_CURVE25519XSALSA20POLY1305_BEFORENMBYTES]);

    verify(sodium)
        .crypto_box_curve25519xsalsa20poly1305_beforenm(
            any(byte[].class), any(byte[].class), any(byte[].class));
  }

  @Test
  public void generateNewKeysSodiumSuccess() {

    when(sodium.crypto_box_curve25519xsalsa20poly1305_keypair(any(byte[].class), any(byte[].class)))
        .thenReturn(1);

    final KeyPair result = kalium.generateNewKeys();

    assertThat(result).isNotNull();
    assertThat(result.getPrivateKey()).isNotNull();
    assertThat(result.getPublicKey()).isNotNull();

    verify(sodium)
        .crypto_box_curve25519xsalsa20poly1305_keypair(any(byte[].class), any(byte[].class));
  }

  @Test
  public void sealAfterPrecomputationSodiumReturnsSuccess() {
    doReturn(1)
        .when(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_afternm(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            eq(sharedKey.getKeyBytes()));

    final byte[] result = kalium.sealAfterPrecomputation(message, nonce, sharedKey);

    assertThat(result).isNotEmpty();

    verify(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_afternm(
            any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class));
  }

  @Test
  public void openUsingSharedKeySodiumReturnsSuccess() {

    final byte[] data = new byte[100];

    doReturn(1)
        .when(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_open_afternm(
            any(byte[].class), eq(data), anyInt(), any(byte[].class), eq(sharedKey.getKeyBytes()));

    final byte[] results = kalium.openAfterPrecomputation(data, nonce, sharedKey);

    assertThat(results).isNotEmpty();

    verify(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_open_afternm(
            any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class));
  }

  @Test
  public void openUsingKeysSodiumReturnsSucesss() {
    final byte[] data = new byte[100];

    doReturn(1)
        .when(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_open(
            any(byte[].class),
            eq(data),
            anyInt(),
            any(byte[].class),
            eq(publicKey.getKeyBytes()),
            eq(privateKey.getKeyBytes()));

    final byte[] result = this.kalium.open(data, nonce, publicKey, privateKey);

    assertThat(result).isNotEmpty();

    verify(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305_open(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            any(byte[].class),
            any(byte[].class));
  }

  @Test
  public void sealUsingKeysSodiumReturnsSuccess() {
    doReturn(1)
        .when(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            eq(publicKey.getKeyBytes()),
            eq(privateKey.getKeyBytes()));

    final byte[] results = this.kalium.seal(message, nonce, publicKey, privateKey);

    assertThat(results).isNotEmpty();

    verify(this.sodium)
        .crypto_box_curve25519xsalsa20poly1305(
            any(byte[].class),
            any(byte[].class),
            anyInt(),
            any(byte[].class),
            any(byte[].class),
            any(byte[].class));
  }

  @Test
  public void generatingRandomKeyReturnsCorrectSize() {
    final int expectedKeysize = 32;

    final SharedKey key = this.kalium.createSingleKey();

    verify(this.sodium).randombytes(any(byte[].class), eq(expectedKeysize));
  }
}
