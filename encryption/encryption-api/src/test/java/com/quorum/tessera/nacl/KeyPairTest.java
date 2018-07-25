package com.quorum.tessera.nacl;

import org.junit.Test;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class KeyPairTest {

    private static final Key TEST_KEY = new Key("test".getBytes(UTF_8));

    private static final Key PRIVATE_KEY = new Key("private".getBytes(UTF_8));

    @Test
    public void differentClassesAreNotEqual() {
        final Object keyPair = new KeyPair(TEST_KEY, TEST_KEY);

        final boolean isEqual = Objects.equals(keyPair, "test");

        assertThat(isEqual).isFalse();
    }

    @Test
    public void differentPublicKeysAreNotEqual() {
        final KeyPair keyPair = new KeyPair(TEST_KEY, PRIVATE_KEY);

        assertThat(keyPair).
            isNotEqualTo(new KeyPair(
                new Key("other".getBytes(UTF_8)),
                PRIVATE_KEY
            ));
    }

    @Test
    public void differentPrivateKeysAreNotEqual() {
        final KeyPair keyPair = new KeyPair(TEST_KEY, PRIVATE_KEY);

        assertThat(keyPair).isNotEqualTo(new KeyPair(TEST_KEY, new Key("private2".getBytes(UTF_8))));
    }

    @Test
    public void equalTest() {
        final KeyPair keyPair = new KeyPair(TEST_KEY, PRIVATE_KEY);

        assertThat(keyPair).isEqualTo(new KeyPair(TEST_KEY, PRIVATE_KEY));
    }

    @Test
    public void sameInstanceIsEqual() {
        final Key key = new Key("bogus".getBytes(UTF_8));
        final KeyPair pair = new KeyPair(key, key);

        assertThat(pair).isEqualTo(pair).isSameAs(pair);
    }

    @Test
    public void hashCodeTest() {
        final Key key = new Key("bogus".getBytes(UTF_8));
        final KeyPair pair = new KeyPair(key, key);

        assertThat(pair).hasSameHashCodeAs(new KeyPair(key, key));
    }

    @Test
    public void toStringTest() {
        final Key key = new Key("bogus".getBytes(UTF_8));
        final KeyPair pair = new KeyPair(key, key);

        assertThat(pair.toString()).isNotBlank();
    }
}
