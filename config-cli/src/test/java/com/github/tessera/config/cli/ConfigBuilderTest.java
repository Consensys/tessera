
package com.github.tessera.config.cli;

import com.github.tessera.config.Config;
import org.junit.Test;


public class ConfigBuilderTest {
    
    @Test
    public void createValidConfig() {
    
        Config config = ConfigBuilder.create()
                .jdbcUrl("jdbcPassword").jdbcUrl("jdbc:bogus").jdbcUsername("jdbcUser").build();
        
    }
    
    
}
