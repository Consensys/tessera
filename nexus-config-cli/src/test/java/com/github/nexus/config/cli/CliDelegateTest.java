
package com.github.nexus.config.cli;

import com.github.nexus.config.Config;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CliDelegateTest {
    
    private CliDelegate configProvider;
    
    public CliDelegateTest() {
    }
    
    @Before
    public void setUp() {
        configProvider = CliDelegate.instance();
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void processArgs() throws Exception {
    
        Config result =     configProvider.execute("-configfile",
                getClass().getResource("/sample-config.json").getFile());
        
        assertThat(result).isNotNull();
        
        
        assertThat(result).isSameAs(configProvider.getConfig());
        
        
    }
    
    @Test(expected = CliException.class)
    public void processArgsMissing() throws Exception {
        configProvider.execute();
    }
    

}
