package com.quorum.tessera.encryption;

import java.util.Arrays;


public interface PrivateKey {
    byte[] getKeyBytes();
    
    static PrivateKey from(byte[] data) {
        return new PrivateKey() {

            @Override
            public byte[] getKeyBytes() {
                return data;
            }

            @Override
            public boolean equals(Object arg0) {
                return PrivateKey.class.isInstance(arg0) 
                        && Arrays.equals(data, ((PrivateKey) arg0).getKeyBytes());
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(data);

            }

        };
    }
}
