package com.quorum.tessera.discovery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DiscoveryHelperFactoryTest {

    private DiscoveryHelperFactory discoveryHelperFactory;

    private DiscoveryHelper discoveryHelper;

    @Before
    public void beforeTest() {
        discoveryHelper = mock(DiscoveryHelper.class);
        discoveryHelperFactory = new DiscoveryHelperFactory(discoveryHelper);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(discoveryHelper);
    }

    @Test
    public void onCreate() {
        discoveryHelperFactory.onCreate();
        verify(discoveryHelper).onCreate();
    }

    @Test
    public void buildCurrent() {
        discoveryHelperFactory.buildCurrent();
        verify(discoveryHelper).buildCurrent();
    }

    @Test
    public void provider() {
        DiscoveryHelper helper = DiscoveryHelperFactory.provider();
        assertThat(helper).isNotNull()
            .isExactlyInstanceOf(DiscoveryHelperImpl.class);

    }


    @Test
    public void defaultConstructor() {
        DiscoveryHelper helper = new DiscoveryHelperFactory();
        assertThat(helper).isNotNull()
            .isExactlyInstanceOf(DiscoveryHelperFactory.class);

    }

}
