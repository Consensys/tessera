package com.quorum.tessera.discovery;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class DiscoveryTest {

    private RuntimeContext runtimeContext;

    @Before
    public void onSetUp() {
        runtimeContext = RuntimeContext.getInstance();
        MockDiscoveryHelper.reset();
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(runtimeContext);
        MockDiscoveryHelper.reset();
    }

    @Test
    public void getInstance() {
        Discovery instance = Discovery.getInstance();
        assertThat(instance).isExactlyInstanceOf(DiscoveryFactory.class);
        verify(runtimeContext).isDisablePeerDiscovery();
    }

    @Test
    public void onCreate() {

        Discovery discovery = new Discovery() {
            @Override
            public void onUpdate(NodeInfo nodeInfo) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void onDisconnect(URI nodeUri) {
                throw new UnsupportedOperationException();

            }
        };

        MockDiscoveryHelper discoveryHelper = MockDiscoveryHelper.class.cast(DiscoveryHelper.getInstance());
        discovery.onCreate();
        assertThat(discoveryHelper.getOnCreateInvocationCount()).isEqualTo(1);
        discovery.onCreate();
        assertThat(discoveryHelper.getOnCreateInvocationCount()).isEqualTo(2);


    }

    @Test
    public void getCurrent() {

        Discovery discovery = new Discovery() {
            @Override
            public void onUpdate(NodeInfo nodeInfo) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void onDisconnect(URI nodeUri) {
                throw new UnsupportedOperationException();

            }
        };

        MockDiscoveryHelper discoveryHelper = MockDiscoveryHelper.class.cast(DiscoveryHelper.getInstance());
        discovery.getCurrent();
        assertThat(discoveryHelper.getBuildCurrentInvocationCounter()).isEqualTo(1);
        discovery.getCurrent();
        assertThat(discoveryHelper.getBuildCurrentInvocationCounter()).isEqualTo(2);


    }

}
