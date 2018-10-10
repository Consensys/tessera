
package com.quorum.tessera.encryption;

import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class KeyFactoryTest {
    
    @Test
    public void createFromStrings() {
    
        List<String> values = Arrays.asList("ONE","TWO");    
    
        List<PublicKey> keys = KeyFactory.convert(values);
        
        assertThat(keys).hasSize(2);
        
        
        
    }
    
}
