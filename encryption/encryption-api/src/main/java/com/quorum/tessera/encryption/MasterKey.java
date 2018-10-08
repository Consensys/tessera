
package com.quorum.tessera.encryption;

import java.util.Arrays;


public interface MasterKey {
    byte[] getKeyBytes();
    
    static MasterKey from(byte[] data) {
        return new MasterKey() {

            @Override
            public byte[] getKeyBytes() {
                return data;
            }

            @Override
            public boolean equals(Object arg0) {
                return MasterKey.class.isInstance(arg0) 
                        && Arrays.equals(data, ((MasterKey) arg0).getKeyBytes());
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(data);

            }

        };
    }
}
