
package com.github.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ConfigBuilderExceptionTest {
 
    @Test
    public void createWithCause() {
        Throwable cause = new UnsupportedOperationException("OUCH");
        ConfigBuilderException exception = new ConfigBuilderException(cause);
        
        assertThat(exception).hasCause(cause);

    }
    
}
