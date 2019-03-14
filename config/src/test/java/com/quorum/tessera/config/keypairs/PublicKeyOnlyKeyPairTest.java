
package com.quorum.tessera.config.keypairs;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class PublicKeyOnlyKeyPairTest {
    
    @Test
    public void createInstance() {
        PublicKeyOnlyKeyPair pair = new PublicKeyOnlyKeyPair("HELLOW");
        pair.withPassword("SomeNotNullValue");
        assertThat(pair.getPublicKey()).isEqualTo("HELLOW");
        assertThat(pair.getPassword()).isNull();
        assertThat(pair.getPrivateKey()).isNull();
        
        
    }
    
}
