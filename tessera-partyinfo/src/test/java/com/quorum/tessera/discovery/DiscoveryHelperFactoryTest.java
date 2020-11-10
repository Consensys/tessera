package com.quorum.tessera.discovery;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Ignore
public class DiscoveryHelperFactoryTest {

    private DiscoveryHelperFactory discoveryHelperFactory;

    private DiscoveryHelper discoveryHelper;

    @Before
    public void beforeTest() {
        discoveryHelper = DiscoveryHelperFactory.provider();
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(discoveryHelper);
    }



    @Test
    public void provider() {
        DiscoveryHelper helper = DiscoveryHelperFactory.provider();
        assertThat(helper).isNotNull().isExactlyInstanceOf(DiscoveryHelperImpl.class);
    }

}
