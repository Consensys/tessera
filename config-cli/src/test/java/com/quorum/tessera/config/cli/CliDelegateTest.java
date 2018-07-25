package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class CliDelegateTest {
    
    private final CliDelegate instance =  CliDelegate.INSTANCE;
    
    @Test
    public void createInstance() {
  
      assertThat(CliDelegate.instance()).isSameAs(instance);
      
    }

     @Test
    public void withValidConfig() throws Exception {

        CliResult result = instance.execute(
            "-configfile",
            getClass().getResource("/sample-config.json").getFile());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig().get()).isSameAs(instance.getConfig());
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isHelpOn()).isFalse();
    }
    

}
