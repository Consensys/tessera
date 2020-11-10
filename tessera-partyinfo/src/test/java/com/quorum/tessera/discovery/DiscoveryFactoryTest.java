package com.quorum.tessera.discovery;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.partyinfo.MockContextHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
@Ignore
public class DiscoveryFactoryTest {

    private RuntimeContext runtimeContext;

    @Before
    public void beforeTest() {
        runtimeContext = RuntimeContext.getInstance();
    }

    @After
    public void afterTest() {
        MockContextHolder.reset();
        verifyNoMoreInteractions(runtimeContext);
    }

    @Test
    public void provideAutoDiscovery() {

        when(runtimeContext.isDisablePeerDiscovery()).thenReturn(false);

        Discovery discovery = DiscoveryFactory.provider();

        assertThat(discovery).isNotNull().isExactlyInstanceOf(AutoDiscovery.class);

        verify(runtimeContext).isDisablePeerDiscovery();
    }

    @Test
    public void provideDisabledAutoDiscovery() {

        when(runtimeContext.isDisablePeerDiscovery()).thenReturn(true);

        Discovery discovery = DiscoveryFactory.provider();

        assertThat(discovery).isNotNull().isExactlyInstanceOf(DisabledAutoDiscovery.class);

        verify(runtimeContext).isDisablePeerDiscovery();
        verify(runtimeContext).getPeers();
    }




    @Test
    public void defaultConstructor() {
        DiscoveryFactory discoveryFactory = new DiscoveryFactory();
        assertThat(discoveryFactory).isNotNull();

        verify(runtimeContext).isDisablePeerDiscovery();

    }

}
