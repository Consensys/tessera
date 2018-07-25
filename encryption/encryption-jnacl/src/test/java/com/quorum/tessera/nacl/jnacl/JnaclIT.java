package com.quorum.tessera.nacl.jnacl;

import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.KeyPair;
import com.quorum.tessera.nacl.Nonce;
import org.junit.Before;
import org.junit.Test;

import java.security.SecureRandom;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class JnaclIT {

    private KeyPair keypairOne;

    private KeyPair keypairTwo;

    private Jnacl jnacl;

    @Before
    public void init() {
        this.jnacl = new Jnacl(new SecureRandom(), new JnaclSecretBox());

        this.keypairOne = jnacl.generateNewKeys();
        this.keypairTwo = jnacl.generateNewKeys();
    }

    @Test
    public void sharedKeyPubaprivbEqualsPrivapubb() {

        final Key sharedKey = jnacl.computeSharedKey(keypairOne.getPublicKey(), keypairTwo.getPrivateKey());

        final Key secondSharedKey = jnacl.computeSharedKey(keypairTwo.getPublicKey(), keypairOne.getPrivateKey());

        assertThat(sharedKey).isEqualTo(secondSharedKey);

    }

    @Test
    public void encryptAndDecryptPayloadUsingSameKeys() {
        final String payload = "Hello world";

        final Key sharedKey = jnacl.computeSharedKey(keypairOne.getPublicKey(), keypairTwo.getPrivateKey());
        final byte[] payloadBytes = payload.getBytes(UTF_8);
        final Nonce nonce = jnacl.randomNonce();

        final byte[] encryptedPayload = jnacl.sealAfterPrecomputation(payloadBytes, nonce, sharedKey);
        final byte[] decryptedPayload = jnacl.openAfterPrecomputation(encryptedPayload, nonce, sharedKey);

        final String decryptedMessage = new String(decryptedPayload, UTF_8);

        assertThat(decryptedMessage).isEqualTo(payload);
    }

    @Test
    public void encryptDecrpytWithoutPrecomputation() {
        final String payload = "Hello world";

        final byte[] payloadBytes = payload.getBytes(UTF_8);
        final Nonce nonce = jnacl.randomNonce();

        final byte[] encryptedPayload = jnacl.seal(payloadBytes, nonce, keypairOne.getPublicKey(), keypairTwo.getPrivateKey());
        final byte[] decryptedPayload = jnacl.open(encryptedPayload, nonce, keypairTwo.getPublicKey(), keypairOne.getPrivateKey());

        final String decryptedMessage = new String(decryptedPayload, UTF_8);

        assertThat(decryptedMessage).isEqualTo(payload);
    }

    @Test
    public void randomKeyCanEncryptAndDecrpytPayload() {

        final String payload = "Hello world";
        final byte[] payloadBytes = payload.getBytes(UTF_8);
        final Nonce nonce = jnacl.randomNonce();

        final Key symmentricKey = jnacl.createSingleKey();

        final byte[] encryptedPayload = jnacl.sealAfterPrecomputation(payloadBytes, nonce, symmentricKey);
        final byte[] decryptedPayload = jnacl.openAfterPrecomputation(encryptedPayload, nonce, symmentricKey);

        final String decryptedMessage = new String(decryptedPayload, UTF_8);
        assertThat(decryptedMessage).isEqualTo(payload);
    }

}
