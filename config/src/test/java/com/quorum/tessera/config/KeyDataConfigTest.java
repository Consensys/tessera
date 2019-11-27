
package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class KeyDataConfigTest {
    
    @Test
    public void getValueWithNoPrivateKeyDataReturnsNull() {
        
        KeyDataConfig keyDataConfig = new KeyDataConfig();
        assertThat(keyDataConfig.getValue()).isNull();
    }
    
    @Test
    public void getValueWithPrivateKeyDataReturns() {
        
        PrivateKeyData privateKeyData = mock(PrivateKeyData.class);
        when(privateKeyData.getValue()).thenReturn("Hellow");
        
        KeyDataConfig keyDataConfig = new KeyDataConfig(privateKeyData, PrivateKeyType.LOCKED);
        
        assertThat(keyDataConfig.getValue()).isEqualTo("Hellow");
        
    }
    
}
