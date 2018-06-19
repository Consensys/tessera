
package com.github.nexus.socket;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class NexusSocketExceptionTest {
    
    public NexusSocketExceptionTest() {
    }

    @Test
    public void createWithCause() {
        
        UnsupportedOperationException cause = new UnsupportedOperationException("OUCH");
        NexusSocketException exception = new NexusSocketException(cause);
        assertThat(exception).hasCause(cause);
    }
    
}
