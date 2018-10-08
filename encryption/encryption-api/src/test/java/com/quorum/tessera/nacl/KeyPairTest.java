package com.quorum.tessera.nacl;

import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;


import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class KeyPairTest {

    private static final PublicKey TEST_KEY = PublicKey.from("test".getBytes(UTF_8));

    private static final PrivateKey PRIVATE_KEY = PrivateKey.from("private".getBytes(UTF_8));



    @Test
    public void differentPublicKeysAreNotEqual() {
        final KeyPair keyPair = new KeyPair(TEST_KEY, PRIVATE_KEY);

        assertThat(keyPair).
            isNotEqualTo(new KeyPair(
                PublicKey.from("other".getBytes(UTF_8)),
                PRIVATE_KEY
            ));
    }



    @Test
    public void equalTest() {
        final KeyPair keyPair = new KeyPair(TEST_KEY, PRIVATE_KEY);

        assertThat(keyPair).isEqualTo(new KeyPair(TEST_KEY, PRIVATE_KEY));
    }

}
