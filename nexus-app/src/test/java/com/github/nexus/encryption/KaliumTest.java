package com.github.nexus.encryption;

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

    private byte[] publicKey = "publickey".getBytes(UTF_8);

    private byte[] privateKey = "privateKey".getBytes(UTF_8);

    private byte[] sharedKey = "sharedKey".getBytes(UTF_8);

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
    public void sodium_is_initialised_on_startup() {
        final NaCl.Sodium sodium = mock(NaCl.Sodium.class);

        final Kalium kalium = new Kalium(sodium);

        verify(sodium).sodium_init();
    }

    @Test
    public void computing_shared_key_throws_exception_on_failure() {
        doReturn(-1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305_beforenm(any(byte[].class), eq(publicKey), eq(privateKey));

        final Throwable kaclEx = catchThrowable(() -> this.kalium.computeSharedKey(publicKey, privateKey));

        assertThat(kaclEx).isInstanceOf(NaclException.class).hasMessage("Kalium could not compute the shared key");

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_beforenm(any(byte[].class), eq(publicKey), eq(privateKey));
    }

    @Test
    public void seal_using_keys_throws_exception_on_failure() {
        doReturn(-1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305(
                        any(byte[].class), any(byte[].class), anyInt(), eq(nonce), eq(publicKey), eq(privateKey)
                );

        final Throwable kaclEx = catchThrowable(() -> this.kalium.seal(message, nonce, publicKey, privateKey));

        assertThat(kaclEx).isInstanceOf(NaclException.class).hasMessage("Kalium could not seal the payload using the provided keys directly");

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305(
                any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class), any(byte[].class)
        );
    }

    @Test
    public void open_using_keys_throws_exception_on_failure() {
        doReturn(-1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305_open(
                        any(byte[].class), eq(message), anyInt(), eq(nonce), eq(publicKey), eq(privateKey)
                );

        final Throwable kaclEx = catchThrowable(() -> this.kalium.open(message, nonce, publicKey, privateKey));

        assertThat(kaclEx).isInstanceOf(NaclException.class).hasMessage("Kalium could not open the payload using the provided keys directly");

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_open(
                any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class), any(byte[].class)
        );
    }

    @Test
    public void seal_using_sharedkey_throws_exception_on_failure() {
        doReturn(-1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305_afternm(
                        any(byte[].class), any(byte[].class), anyInt(), eq(nonce), eq(sharedKey)
                );

        final Throwable kaclEx = catchThrowable(() -> this.kalium.sealAfterPrecomputation(message, nonce, sharedKey));

        assertThat(kaclEx).isInstanceOf(NaclException.class).hasMessage("Kalium could not seal the payload using the shared key");

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_afternm(
                any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class)
        );
    }

    @Test
    public void open_using_sharedkey_throws_exception_on_failure() {
        doReturn(-1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305_open_afternm(
                        any(byte[].class), eq(message), anyInt(), eq(nonce), eq(sharedKey)
                );

        final Throwable kaclEx = catchThrowable(() -> this.kalium.openAfterPrecomputation(message, nonce, sharedKey));

        assertThat(kaclEx).isInstanceOf(NaclException.class).hasMessage("Kalium could not open the payload using the shared key");

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_open_afternm(
                any(byte[].class), any(byte[].class), anyInt(), any(byte[].class), any(byte[].class)
        );
    }

    @Test
    public void nonce_contains_random_data() {
        this.kalium.randomNonce();

        verify(this.sodium).randombytes(any(byte[].class), anyInt());
    }

    @Test
    public void generating_new_keys_throws_exception_on_failure() {
        doReturn(-1)
                .when(this.sodium)
                .crypto_box_curve25519xsalsa20poly1305_keypair(any(byte[].class), any(byte[].class));

        final Throwable kaclEx = catchThrowable(() -> this.kalium.generateNewKeys());

        assertThat(kaclEx).isInstanceOf(NaclException.class).hasMessage("Kalium could not generate a new public/private keypair");

        verify(this.sodium).crypto_box_curve25519xsalsa20poly1305_keypair(any(byte[].class), any(byte[].class));
    }


}
