package com.quorum.tessera.enclave.websockets;

import java.net.URI;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class UriPathAppenderTest {
    
    @Test
    public void createFromServerUri() {
        URI myuri = URI.create("ws:/foo.bar:9099");
        
        URI result = UriPathAppender.createFromServerUri(myuri);
        assertThat(result.toString()).isEqualTo("ws:/foo.bar:9099/enclave");
        
    }
    
}
