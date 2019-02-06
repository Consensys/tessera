package com.jpmorgan.quorum.enclave.websockets;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class InterruptableCallbackTest {
    
    @Test
    public void notMuchThatCanBeDone() {
        
        InterruptableCallback.execute(() -> {
            throw new InterruptedException("BANG");
        });
        
        assertThat(true)
                .describedAs("InterruptedException should be ingored")
                .isTrue();
        
    }
    
    
}
