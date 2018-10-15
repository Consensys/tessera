
package com.quorum.tessera.encryption;

import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Stream;


public abstract class BaseKey implements Key {
    
    private final byte[] keyBytes;

    private final String base64Encoded;
    
    protected BaseKey(byte[] keyBytes) {
        this.keyBytes = keyBytes;
        this.base64Encoded = Base64.getEncoder().encodeToString(keyBytes);
    }

    @Override
    public final byte[] getKeyBytes() {
        return keyBytes;
    }

    @Override
    public String encodeToBase64() {
        return base64Encoded;
    }

    
    @Override
    public final boolean equals(Object arg0) {
        return getClass().isInstance(arg0)
                && Arrays.equals(keyBytes, getClass().cast(arg0).getKeyBytes());
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(keyBytes);
    }

    @Override
    public final String toString() {
        
        final String typeName = Stream.of(getClass())
                .map(Class::getInterfaces)
                .flatMap(Stream::of)
                .map(Class::getSimpleName)
                .findFirst()
                .get();
        
        return String.format("%s[%s]",typeName,encodeToBase64());
    }

    
    
    
}
