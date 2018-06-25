
package com.github.nexus.config.jaxb;

import com.github.nexus.config.SslAuthenticationMode;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SslAuthenticationModeAdapterTest {
    
    private SslAuthenticationModeAdapter sslAuthenticationModeAdapter;
    
    public SslAuthenticationModeAdapterTest() {
    }
    
    @Before
    public void setUp() {
        sslAuthenticationModeAdapter = new SslAuthenticationModeAdapter();
    }
    
    @After
    public void tearDown() {
        sslAuthenticationModeAdapter = null;
    }
    

    @Test
    public void marshal() throws Exception {

        for (SslAuthenticationMode mode : SslAuthenticationMode.values()) {
            String result = sslAuthenticationModeAdapter.marshal(mode);
            assertThat(result).isEqualTo(mode.name());
        }
    }
    
    @Test
    public void unmarshal() throws Exception {

        for (SslAuthenticationMode mode : SslAuthenticationMode.values()) {
            SslAuthenticationMode result = sslAuthenticationModeAdapter.unmarshal(mode.name());
            assertThat(result).isSameAs(mode);
        }
    }
    
}
