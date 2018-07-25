package com.quorum.tessera.nacl.kalium;

import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.KeyPair;
import com.quorum.tessera.nacl.Nonce;
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
    public void sharedKeyPubaprivbEqualsPrivapubb() {

        final Key sharedKey = kalium.computeSharedKey(keypairOne.getPublicKey(), keypairTwo.getPrivateKey());

        final Key secondSharedKey = kalium.computeSharedKey(keypairTwo.getPublicKey(), keypairOne.getPrivateKey());

        assertThat(sharedKey).isEqualTo(secondSharedKey);

    }

    @Test
    public void encryptAndDecryptPayloadUsingSameKeys() {
        final String payload = "Hello world";

        final Key sharedKey = kalium.computeSharedKey(keypairOne.getPublicKey(), keypairTwo.getPrivateKey());
        final byte[] payloadBytes = payload.getBytes(UTF_8);
        final Nonce nonce = kalium.randomNonce();

        final byte[] encryptedPayload = kalium.sealAfterPrecomputation(payloadBytes, nonce, sharedKey);
        final byte[] decryptedPayload = kalium.openAfterPrecomputation(encryptedPayload, nonce, sharedKey);

        final String decryptedMessage = new String(decryptedPayload, UTF_8);

        assertThat(decryptedMessage).isEqualTo(payload);
    }

    @Test
    public void encryptDecrpytWithoutPrecomputation() {
        final String payload = "Hello world";

        final byte[] payloadBytes = payload.getBytes(UTF_8);
        final Nonce nonce = kalium.randomNonce();

        final byte[] encryptedPayload = kalium.seal(payloadBytes, nonce, keypairOne.getPublicKey(), keypairTwo.getPrivateKey());
        final byte[] decryptedPayload = kalium.open(encryptedPayload, nonce, keypairTwo.getPublicKey(), keypairOne.getPrivateKey());

        final String decryptedMessage = new String(decryptedPayload, UTF_8);

        assertThat(decryptedMessage).isEqualTo(payload);
    }

    @Test
    public void randomKeyCanEncryptAndDecrpytPayload() {

        final String payload = "Hello world";
        final byte[] payloadBytes = payload.getBytes(UTF_8);
        final Nonce nonce = kalium.randomNonce();

        final Key symmentricKey = kalium.createSingleKey();

        final byte[] encryptedPayload = kalium.sealAfterPrecomputation(payloadBytes, nonce, symmentricKey);
        final byte[] decryptedPayload = kalium.openAfterPrecomputation(encryptedPayload, nonce, symmentricKey);

        final String decryptedMessage = new String(decryptedPayload, UTF_8);
        assertThat(decryptedMessage).isEqualTo(payload);
    }

}
