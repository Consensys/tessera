
package com.quorum.tessera.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class ReceiveResponseTest {
    
    @Test
    public void createInstanceWithPayload() {
        String payload = "HELLOW";
        ReceiveResponse instance = new ReceiveResponse(payload);
        
        assertThat(instance.getPayload()).isEqualTo(payload);
        
    }
    
}
