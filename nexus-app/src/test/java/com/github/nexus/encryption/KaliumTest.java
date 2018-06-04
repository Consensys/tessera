package com.github.nexus.encryption;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.keys.model.KeyPair;
import org.abstractj.kalium.NaCl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class KaliumTest {

    private Key publicKey = new Key("publickey".getBytes(UTF_8));

    private Key privateKey = new Key("privateKey".getBytes(UTF_8));

    private Key sharedKey = new Key("sharedKey".getBytes(UTF_8));

    private byte[] message = "TEST_MESSAGE".getBytes(UTF_8);

    private byte[] nonce = "TEST_NONCE".getBytes(UTF_8);

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

        final Kalium kalium = new Kalium(sodium);

        verify(sodium).sodium_init();
    }

    @Test
    public void computingSharedKeyThrowsExceptionOnFailure() {
        doReturn(-1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305_beforenm(any(byte[].class), eq(publicKey.getKeyBytes()), eq(privateKey.getKeyBytes()));

        final Throwable kaclEx = catchThrowable(() -> this.kalium.computeSharedKey(publicKey, privateKey));

        assertThat(kaclEx).isInstanceOf(NaclException.class).hasMessage("Kalium could not compute the shared key");

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_beforenm(any(byte[].class), eq(publicKey.getKeyBytes()), eq(privateKey.getKeyBytes()));
    }

    @Test
    public void sealUsingKeysThrowsExceptionOnFailure() {
        doReturn(-1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305(
                        any(byte[].class), any(byte[].class), anyInt(), eq(nonce), eq(publicKey.getKeyBytes()), eq(privateKey.getKeyBytes())
                );

        final Throwable kaclEx = catchThrowable(() -> this.kalium.seal(message, nonce, publicKey, privateKey));

        assertThat(kaclEx).isInstanceOf(NaclException.class).hasMessage("Kalium could not seal the payload using the provided keys directly");

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305(
                any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class), any(byte[].class)
        );
    }

    @Test
    public void openUsingKeysThrowsExceptionOnFailure() {
        doReturn(-1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305_open(
                        any(byte[].class), eq(message), anyInt(), eq(nonce), eq(publicKey.getKeyBytes()), eq(privateKey.getKeyBytes())
                );

        final Throwable kaclEx = catchThrowable(() -> this.kalium.open(message, nonce, publicKey, privateKey));

        assertThat(kaclEx).isInstanceOf(NaclException.class).hasMessage("Kalium could not open the payload using the provided keys directly");

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_open(
                any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class), any(byte[].class)
        );
    }

    @Test
    public void sealUsingSharedkeyThrowsExceptionOnFailure() {
        doReturn(-1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305_afternm(
                        any(byte[].class), any(byte[].class), anyInt(), eq(nonce), eq(sharedKey.getKeyBytes())
                );

        final Throwable kaclEx = catchThrowable(() -> this.kalium.sealAfterPrecomputation(message, nonce, sharedKey));

        assertThat(kaclEx).isInstanceOf(NaclException.class).hasMessage("Kalium could not seal the payload using the shared key");

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_afternm(
                any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class)
        );
    }

    @Test
    public void openUsingSharedkeyThrowsExceptionOnFailure() {
        doReturn(-1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305_open_afternm(
                        any(byte[].class), eq(message), anyInt(), eq(nonce), eq(sharedKey.getKeyBytes())
                );

        final Throwable kaclEx = catchThrowable(() -> this.kalium.openAfterPrecomputation(message, nonce, sharedKey));

        assertThat(kaclEx).isInstanceOf(NaclException.class).hasMessage("Kalium could not open the payload using the shared key");

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_open_afternm(
                any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class)
        );
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

        assertThat(kaclEx).isInstanceOf(NaclException.class).hasMessage("Kalium could not generate a new public/private keypair");

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_keypair(any(byte[].class), any(byte[].class));
    }

    @Test
    public void computeSharedKeySodiumReturnsSuccess() {

        when(sodium.crypto_box_curve25519xsalsa20poly1305_beforenm(any(byte[].class),
                any(byte[].class), any(byte[].class))).thenReturn(1);

        Key result = kalium.computeSharedKey(publicKey, privateKey);

        assertThat(result).isNotNull();
        assertThat(result.getKeyBytes())
                .isEqualTo(new byte[NaCl.Sodium.CRYPTO_BOX_CURVE25519XSALSA20POLY1305_BEFORENMBYTES]);

        verify(sodium).crypto_box_curve25519xsalsa20poly1305_beforenm(
                any(byte[].class), any(byte[].class), any(byte[].class));

    }

    @Test
    public void generateNewKeysSodiumSucess() {

        when(sodium.crypto_box_curve25519xsalsa20poly1305_keypair(
                any(byte[].class), any(byte[].class))).thenReturn(1);

        KeyPair result = kalium.generateNewKeys();
        assertThat(result).isNotNull();
        assertThat(result.getPrivateKey()).isNotNull();
        assertThat(result.getPublicKey()).isNotNull();

        verify(sodium).crypto_box_curve25519xsalsa20poly1305_keypair(any(byte[].class), any(byte[].class));

    }

    @Test
    public void sealAfterPrecomputationSodiumReturnsSuccess() {
        doReturn(1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305_afternm(
                        any(byte[].class), any(byte[].class), anyInt(), eq(nonce), eq(sharedKey.getKeyBytes())
                );

        byte[] result = kalium.sealAfterPrecomputation(message, nonce, sharedKey);

        assertThat(result).isNotEmpty();

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_afternm(
                any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class)
        );
    }

    @Test
    public void openUsingSharedKeySodiumReturnsSuccess() {

        byte[] data = new byte[100];

        doReturn(1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305_open_afternm(
                        any(byte[].class), eq(data), anyInt(), eq(nonce), eq(sharedKey.getKeyBytes())
                );

        byte[] results = kalium.openAfterPrecomputation(data, nonce, sharedKey);

        assertThat(results).isNotEmpty();

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_open_afternm(
                any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class)
        );
    }

    @Test
    public void openUsingKeysSodiumReturnsSucesss() {
        byte[] data = new byte[100];

        doReturn(1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305_open(
                        any(byte[].class), eq(data), anyInt(), eq(nonce), eq(publicKey.getKeyBytes()), eq(privateKey.getKeyBytes())
                );

        byte[] result = this.kalium.open(data, nonce, publicKey, privateKey);

        assertThat(result).isNotEmpty();

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_open(
                any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class), any(byte[].class)
        );
    }
    
    @Test
    public void sealUsingKeysSodiumReturnsSucess() {
        doReturn(1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305(
                        any(byte[].class), any(byte[].class), anyInt(), eq(nonce), eq(publicKey.getKeyBytes()), eq(privateKey.getKeyBytes())
                );

        byte[] results = this.kalium.seal(message, nonce, publicKey, privateKey);


        assertThat(results).isNotEmpty();

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305(
                any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class), any(byte[].class)
        );
    }

}
