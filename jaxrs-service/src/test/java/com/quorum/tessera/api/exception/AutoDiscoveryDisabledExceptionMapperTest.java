package com.quorum.tessera.api.exception;

import com.quorum.tessera.node.AutoDiscoveryDisabledException;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

public class AutoDiscoveryDisabledExceptionMapperTest {

    private AutoDiscoveryDisabledExceptionMapper mapper;

    @Before
    public void onSetUp() {
        mapper = new AutoDiscoveryDisabledExceptionMapper();
    }

    @Test
    public void handleAutoDiscoveryDisabledException() {
        String message = ".. all outta gum";
        AutoDiscoveryDisabledException exception = 
                new AutoDiscoveryDisabledException(message);

        Response result = mapper.toResponse(exception);
        
        assertThat(result.getStatus()).isEqualTo(403);
        assertThat(result.getEntity()).isEqualTo(message);
    }

}
