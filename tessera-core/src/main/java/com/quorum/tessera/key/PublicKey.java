package com.quorum.tessera.key;

import java.util.Arrays;

public interface PublicKey {

    byte[] getKeyBytes();

    static PublicKey from(byte[] data) {
        return new PublicKey() {

            @Override
            public byte[] getKeyBytes() {
                return data;
            }

            @Override
            public boolean equals(Object arg0) {
                return PublicKey.class.isInstance(arg0) 
                        && Arrays.equals(data, ((PublicKey) arg0).getKeyBytes());
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(data);

            }

        };
    }

}
