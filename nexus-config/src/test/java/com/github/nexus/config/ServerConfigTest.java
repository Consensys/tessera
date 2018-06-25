
package com.github.nexus.config;

import java.net.URI;
import java.net.URISyntaxException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ServerConfigTest {
    
    public ServerConfigTest() {
    }
    

    @Test
    public void defaultUri() throws URISyntaxException {
        
        final ServerConfig serverConfig = new ServerConfig() {
            @Override
            public int getPort() {
                return 9999;
            }

            @Override
            public SslConfig getSslConfig() {
                return null;
            }

            @Override
            public String getHostName() {
                return "bogus";
            }
        };
        
        assertThat(serverConfig.getServerUri()).isEqualTo(new URI("bogus:9999"));
        
    }
    
    @Test(expected = ConfigException.class)
    public void defaultUriInvalid() throws URISyntaxException {
        
        final ServerConfig serverConfig = new ServerConfig() {
            @Override
            public int getPort() {
                return 9999;
            }

            @Override
            public SslConfig getSslConfig() {
                return null;
            }

            @Override
            public String getHostName() {
                return "bogus$*>,~";
            }
        };
 
       
        
        serverConfig.getServerUri();
        
    }
    
}
