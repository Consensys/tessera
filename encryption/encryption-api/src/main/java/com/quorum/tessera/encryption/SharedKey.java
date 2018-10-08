
package com.quorum.tessera.encryption;

import java.util.Arrays;


public interface SharedKey {
     byte[] getKeyBytes();
     
    static SharedKey from(byte[] data) {
        return new SharedKey() {

            @Override
            public byte[] getKeyBytes() {
                return data;
            }

            @Override
            public boolean equals(Object arg0) {
                return SharedKey.class.isInstance(arg0) 
                        && Arrays.equals(data, ((SharedKey) arg0).getKeyBytes());
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(data);

            }

        };
    }
}
