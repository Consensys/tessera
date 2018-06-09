package com.github.nexus.nacl;

import org.junit.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyPairTest {

    @Test
    public void differentClassesAreNotEqual() {
        final KeyPair keyPair = new KeyPair(
                new Key("test".getBytes()),
                new Key("test".getBytes())
        );

        final boolean isEqual = Objects.equals(keyPair, "test");

        assertThat(isEqual).isFalse();
    }

    @Test
    public void differentPublicKeysAreNotEqual() {
        final KeyPair keyPair = new KeyPair(
                new Key("test".getBytes()),
                new Key("private".getBytes())
        );

        assertThat(keyPair).
                isNotEqualTo(new KeyPair(
                        new Key("other".getBytes()),
                        new Key("private".getBytes())
                ));
    }
    
        @Test
    public void differentPrivateKeysAreNotEqual() {
        final KeyPair keyPair = new KeyPair(
                new Key("test".getBytes()),
                new Key("private".getBytes())
        );

        assertThat(keyPair).
                isNotEqualTo(new KeyPair(
                        new Key("test".getBytes()),
                        new Key("private2".getBytes())
                ));
    }

    @Test
    public void equalTest() {
        final KeyPair keyPair = new KeyPair(
                new Key("test".getBytes()),
                new Key("private".getBytes())
        );


        assertThat(keyPair).
                isEqualTo(new KeyPair(
                        new Key("test".getBytes()),
                        new Key("private".getBytes())
                ));
    }

    @Test
    public void sameInstanceIsEqual() {
        Key key = new Key("bogus".getBytes());
        KeyPair pair = new KeyPair(key, key);

        assertThat(pair).isEqualTo(pair).isSameAs(pair);
    }

    @Test
    public void hashCodeTest() {
        Key key = new Key("bogus".getBytes());
        KeyPair pair = new KeyPair(key, key);
        assertThat(pair)
                .hasSameHashCodeAs(new KeyPair(key, key));

    }

    @Test
    public void toStringTest() {
        Key key = new Key("bogus".getBytes());
        KeyPair pair = new KeyPair(key, key);
        assertThat(pair.toString())
                .isNotBlank();

    }
}
