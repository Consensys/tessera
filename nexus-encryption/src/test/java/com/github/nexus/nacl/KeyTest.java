package com.github.nexus.nacl;

import org.junit.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyTest {

    @Test
    public void differentClassesAreNotEqual() {
        final boolean isEqual = Objects.equals(new Key("test".getBytes()), "test");

        assertThat(isEqual).isFalse();
    }

    @Test
    public void sameInstanceIsEqual() {
        Key key = new Key("bogus".getBytes());
        assertThat(key).isEqualTo(key).isSameAs(key);
    }

    @Test
    public void getKeyBytes() {
        byte[] data = "bogus".getBytes();
        final Key key = new Key(data);

        
        assertThat(key.getKeyBytes())
                .isEqualTo(data)
                .isNotSameAs(data);

    }
    
    @Test
    public void hashCodeTest() {
        byte[] data = "bogus".getBytes();
        final Key key = new Key(data);
        assertThat(key)
                .hasSameHashCodeAs(new Key(data));
        
    }
    
    @Test
    public void toStringTest() {
        byte[] data = "bogus".getBytes();
        final Key key = new Key(data);
        
        assertThat(key.toString())
                .isNotBlank();
    }
    
}
