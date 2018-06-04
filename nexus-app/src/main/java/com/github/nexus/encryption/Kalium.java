package com.github.nexus.encryption;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.keys.model.KeyPair;
import org.abstractj.kalium.NaCl;

import java.util.Objects;

import static org.abstractj.kalium.NaCl.Sodium.*;

/**
 * An implementation of the {@link NaclFacade} using the Kalium and libsodium binding
 */
public class Kalium implements NaclFacade {

    private final NaCl.Sodium sodium;

    public Kalium(final NaCl.Sodium sodium) {
        this.sodium = Objects.requireNonNull(sodium, "Kalium sodium implementation was null");
        this.sodium.sodium_init();
    }

    @Override
    public Key computeSharedKey(final Key publicKey, final Key privateKey) {
        final byte[] output = new byte[NaCl.Sodium.CRYPTO_BOX_CURVE25519XSALSA20POLY1305_BEFORENMBYTES];

        final int sodiumResult = this.sodium.crypto_box_curve25519xsalsa20poly1305_beforenm(
                output, publicKey.getKeyBytes(), privateKey.getKeyBytes()
        );

        if(sodiumResult == -1) {
            throw new NaclException("Kalium could not compute the shared key");
        }

        return new Key(output);
    }

    @Override
    public byte[] seal(final byte[] message, final byte[] nonce, final Key publicKey, final Key privateKey) {
        /*
         * The Kalium library uses the C API
         * which expects the first CRYPTO_BOX_CURVE25519XSALSA20POLY1305_ZEROBYTES bytes to be zero
         */
        final byte[] paddedMessage = pad(message, CRYPTO_BOX_CURVE25519XSALSA20POLY1305_ZEROBYTES);
        final byte[] output = new byte[paddedMessage.length];

        final int sodiumResult = sodium.crypto_box_curve25519xsalsa20poly1305(
                output, paddedMessage, paddedMessage.length, nonce, publicKey.getKeyBytes(), privateKey.getKeyBytes()
        );

        if(sodiumResult == -1) {
            throw new NaclException("Kalium could not seal the payload using the provided keys directly");
        }

        return output;
    }

    @Override
    public byte[] open(final byte[] cipherText, final byte[] nonce, final Key publicKey, final Key privateKey) {
        final byte[] paddedOutput = new byte[cipherText.length];

        final int sodiumResult = sodium.crypto_box_curve25519xsalsa20poly1305_open(
                paddedOutput, cipherText, cipherText.length, nonce, publicKey.getKeyBytes(), privateKey.getKeyBytes()
        );

        if(sodiumResult == -1) {
            throw new NaclException("Kalium could not open the payload using the provided keys directly");
        }

        return extract(paddedOutput, CRYPTO_BOX_CURVE25519XSALSA20POLY1305_ZEROBYTES);
    }

    @Override
    public byte[] sealAfterPrecomputation(final byte[] message, final byte[] nonce, final Key sharedKey) {
        /*
         * The Kalium library uses the C API
         * which expects the first CRYPTO_BOX_CURVE25519XSALSA20POLY1305_ZEROBYTES bytes to be zero
         */
        final byte[] paddedMessage = pad(message, CRYPTO_BOX_CURVE25519XSALSA20POLY1305_ZEROBYTES);
        final byte[] output = new byte[paddedMessage.length];

        final int sodiumResult = this.sodium.crypto_box_curve25519xsalsa20poly1305_afternm(
                output, paddedMessage, paddedMessage.length, nonce, sharedKey.getKeyBytes()
        );

        if(sodiumResult == -1) {
            throw new NaclException("Kalium could not seal the payload using the shared key");
        }

        return output;
    }

    @Override
    public byte[] openAfterPrecomputation(final byte[] encryptedPayload, final byte[] nonce, final Key sharedKey) {
        final byte[] paddedOutput = new byte[encryptedPayload.length];

        final int sodiumResult = this.sodium.crypto_box_curve25519xsalsa20poly1305_open_afternm(
                paddedOutput, encryptedPayload, encryptedPayload.length, nonce, sharedKey.getKeyBytes()
        );

        if(sodiumResult == -1) {
            throw new NaclException("Kalium could not open the payload using the shared key");
        }

        return extract(paddedOutput, CRYPTO_BOX_CURVE25519XSALSA20POLY1305_ZEROBYTES);
    }

    @Override
    public byte[] randomNonce() {
        final byte[] nonce = new byte[CRYPTO_BOX_CURVE25519XSALSA20POLY1305_NONCEBYTES];

        this.sodium.randombytes(nonce, nonce.length);

        return nonce;
    }

    @Override
    public KeyPair generateNewKeys() {
        final byte[] publicKey = new byte[CRYPTO_BOX_CURVE25519XSALSA20POLY1305_PUBLICKEYBYTES];
        final byte[] privateKey = new byte[CRYPTO_BOX_CURVE25519XSALSA20POLY1305_SECRETKEYBYTES];

        final int sodiumResult = this.sodium.crypto_box_curve25519xsalsa20poly1305_keypair(publicKey, privateKey);

        if(sodiumResult == -1) {
            throw new NaclException("Kalium could not generate a new public/private keypair");
        }

        return new KeyPair(new Key(publicKey), new Key(privateKey));
    }

    /**
     * Left-pads a given message with padSize amount of zeros
     *
     * @param input the message to be padded
     * @param padSize the amount of left-padding to apply
     * @return the padded message
     */
    private byte[] pad(final byte[] input, final int padSize) {
        final byte[] paddedMessage = new byte[padSize + input.length];
        System.arraycopy(input, 0, paddedMessage, padSize, input.length);

        return paddedMessage;
    }

    /**
     * Removes left-padding from a given message to tune of padSize
     *
     * @param input The message from which to remove left-padding
     * @param padSize The amount of left-padding to remove
     * @return The trimmed message
     */
    private byte[] extract(final byte[] input, final int padSize) {
        final byte[] extractedMessage = new byte[input.length - padSize];
        System.arraycopy(input, padSize, extractedMessage, 0, extractedMessage.length);

        return extractedMessage;
    }

}