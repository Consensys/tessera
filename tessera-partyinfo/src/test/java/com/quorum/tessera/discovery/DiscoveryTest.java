package com.quorum.tessera.discovery;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.net.URI;
import java.util.Optional;
import java.util.ServiceLoader;

import static org.mockito.Mockito.*;

public class DiscoveryTest {

    private Discovery discovery;

    private MockedStatic<DiscoveryHelper> mockedStaticDiscoveryHelper;

    private DiscoveryHelper discoveryHelper;

    @Before
    public void beforeTest() {
        discovery = new Discovery() {
            @Override
            public void onUpdate(NodeInfo nodeInfo) {
            }

            @Override
            public void onDisconnect(URI nodeUri) {
            }
        };

        discoveryHelper = mock(DiscoveryHelper.class);
        mockedStaticDiscoveryHelper = mockStatic(DiscoveryHelper.class);
        mockedStaticDiscoveryHelper.when(DiscoveryHelper::create).thenReturn(discoveryHelper);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(discoveryHelper);
        mockedStaticDiscoveryHelper.verifyNoMoreInteractions();
        mockedStaticDiscoveryHelper.close();
    }

    @Test
    public void onCreate() {
        discovery.onCreate();
        verify(discoveryHelper).onCreate();
        mockedStaticDiscoveryHelper.verify(DiscoveryHelper::create);
    }

    @Test
    public void getCurrent() {
        discovery.getCurrent();
        verify(discoveryHelper).buildCurrent();
        mockedStaticDiscoveryHelper.verify(DiscoveryHelper::create);
    }

    @Test
    public void getRemoteNodeInfo() {
        PublicKey publicKey = mock(PublicKey.class);
        discovery.getRemoteNodeInfo(publicKey);
        verify(discoveryHelper).buildRemoteNodeInfo(publicKey);
        mockedStaticDiscoveryHelper.verify(DiscoveryHelper::create);
    }

    @Test
    public void getRemoteNodeInfos() {
        discovery.getRemoteNodeInfos();
        verify(discoveryHelper).buildRemoteNodeInfos();
        mockedStaticDiscoveryHelper.verify(DiscoveryHelper::create);
    }

    @Test
    public void getInstance() {

        try(var staticServiceLoader = mockStatic(ServiceLoader.class)) {
            final ServiceLoader serviceLoader = mock(ServiceLoader.class);
            when(serviceLoader.findFirst()).thenReturn(Optional.of(mock(Discovery.class)));
            staticServiceLoader.when(() -> ServiceLoader.load(Discovery.class))
                .thenReturn(serviceLoader);

            Discovery.create();
            verify(serviceLoader).findFirst();
            verifyNoMoreInteractions(serviceLoader);

            staticServiceLoader.verify(() -> ServiceLoader.load(Discovery.class));
            staticServiceLoader.verifyNoMoreInteractions();

        }
    }

}
