package com.github.nexus.config.jaxb;

import com.github.nexus.config.SslTrustMode;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SslTrustModeAdapterTest {

    private SslTrustModeAdapter sslTrustModeAdapter;

    public SslTrustModeAdapterTest() {
    }

    @Before
    public void setUp() {
        sslTrustModeAdapter = new SslTrustModeAdapter();
    }

    @After
    public void tearDown() {
        sslTrustModeAdapter = null;
    }

    @Test
    public void marshal() throws Exception {

        for (SslTrustMode mode : SslTrustMode.values()) {
            String result = sslTrustModeAdapter.marshal(mode);
            assertThat(result).isEqualTo(mode.name());
        }
    }
    
    @Test
    public void unmarshal() throws Exception {

        for (SslTrustMode mode : SslTrustMode.values()) {
            SslTrustMode result = sslTrustModeAdapter.unmarshal(mode.name());
            assertThat(result).isSameAs(mode);
        }
    }
}
