package com.github.nexus.encryption;

import org.abstractj.kalium.NaCl;
import org.junit.Before;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class KaliumIT {

    private KeyPair keypairOne;

    private KeyPair keypairTwo;

    private Kalium kalium;

    @Before
    public void init() {
        final NaCl.Sodium sodium = NaCl.sodium();

        this.kalium = new Kalium(sodium);

        this.keypairOne = kalium.generateNewKeys();
        this.keypairTwo = kalium.generateNewKeys();
    }

    @Test
    public void shared_key_pubAprivB_equals_privApubB() {

        final byte[] sharedKey = kalium.computeSharedKey(keypairOne.getPublicKey(), keypairTwo.getPrivateKey());

        final byte[] secondSharedKey = kalium.computeSharedKey(keypairTwo.getPublicKey(), keypairOne.getPrivateKey());

        assertThat(sharedKey).containsExactly(secondSharedKey);

    }

    @Test
    public void encrypt_and_decrypt_payload_using_same_keys() {
        final String payload = "Hello world";

        final byte[] sharedKey = kalium.computeSharedKey(keypairOne.getPublicKey(), keypairTwo.getPrivateKey());
        final byte[] payloadBytes = payload.getBytes(UTF_8);
        final byte[] nonce = kalium.randomNonce();

        final byte[] encryptedPayload = kalium.sealAfterPrecomputation(payloadBytes, nonce, sharedKey);
        final byte[] decryptedPayload = kalium.openAfterPrecomputation(encryptedPayload, nonce, sharedKey);

        final String decryptedMessage = new String(decryptedPayload, UTF_8);

        assertThat(decryptedMessage).isEqualTo(payload);
    }

    @Test
    public void encrypt_decrpyt_without_precomputation() {
        final String payload = "Hello world";

        final byte[] payloadBytes = payload.getBytes(UTF_8);
        final byte[] nonce = kalium.randomNonce();

        final byte[] encryptedPayload = kalium.seal(payloadBytes, nonce, keypairOne.getPublicKey(), keypairTwo.getPrivateKey());
        final byte[] decryptedPayload = kalium.open(encryptedPayload, nonce, keypairTwo.getPublicKey(), keypairOne.getPrivateKey());

        final String decryptedMessage = new String(decryptedPayload, UTF_8);

        assertThat(decryptedMessage).isEqualTo(payload);
    }


}
