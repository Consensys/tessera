package com.quorum.tessera.nacl;

import java.util.Arrays;
import java.util.Base64;

/**
 * Represents a Key, which is usually 32 bytes in length The possible types of
 * keys include: - public - private - symmetric
 */
public class Key {

    private final byte[] keyBytes;

    public Key(final byte[] keyBytes) {
        this.keyBytes = Arrays.copyOf(keyBytes, keyBytes.length);
    }
    
    
    public byte[] getKeyBytes() {
        return Arrays.copyOf(this.keyBytes, this.keyBytes.length);
    }

    @Override
    public boolean equals(final Object o) {
        return (o instanceof Key) && Arrays.equals(keyBytes, ((Key) o).keyBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(keyBytes);
    }

    //FIXME: use getKeyAsString. toString should not be used as an accessor like this.
    @Override
    public String toString() {
        return getKeyAsString();
    }
    
    public String getKeyAsString() {
        return Base64.getEncoder().encodeToString(this.keyBytes);
    }
    
    /*
    As this object is used as a config object 
    we add the factory method so empty instances 
    can be created 
    */
    private static Key create() {
        return new Key(new byte[0]);
    }
}
