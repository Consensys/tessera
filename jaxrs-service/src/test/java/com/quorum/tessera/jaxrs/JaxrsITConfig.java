
package com.quorum.tessera.jaxrs;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.enclave.EnclaveMediator;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import java.nio.file.Path;
import java.util.Collections;
import static org.mockito.Mockito.mock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(locations = "classpath:tessera-jaxrs-spring.xml")
public class JaxrsITConfig {
    
    @Bean(name = "enclaveDelegate")    
    public EnclaveMediator enclaveMediator() {
        return mock(EnclaveMediator.class);
    }
    
    @Bean
    public PartyInfoService partyInfoService() {
        return mock(PartyInfoService.class);
    }
    
    @Bean
    public PartyInfoParser partyInfoParser() {
        return mock(PartyInfoParser.class);
    }
    
    @Bean
    public Config config() {

        JdbcConfig jdbcConfig = mock(JdbcConfig.class);
        
        ServerConfig serverConfig = mock(ServerConfig.class);
        
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        
        Path unixSocketFile = mock(Path.class);
                
         Config config = new Config(jdbcConfig,serverConfig,Collections.EMPTY_LIST,keyConfiguration,Collections.EMPTY_LIST,unixSocketFile,false,false);
        
        return config;
    }
    
}
