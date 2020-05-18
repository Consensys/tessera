package com.quorum.tessera.launcher;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;

public class MockTesseraServerFactory implements TesseraServerFactory {

    private final List<TesseraServer> holder = new ArrayList<>();

    private static MockTesseraServerFactory instance = new MockTesseraServerFactory();

    public MockTesseraServerFactory() {
    }

    @Override
    public TesseraServer createServer(ServerConfig config, Set services) {
        return instance.create();
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }

    private TesseraServer create() {
        final TesseraServer mockServer = mock(TesseraServer.class);
        instance.getHolder().add(mockServer);
        return mockServer;
    }


    public List<TesseraServer> getHolder() {
        return getInstance().holder;
    }

    public static MockTesseraServerFactory getInstance() {
        if(instance == null) {
            instance = new MockTesseraServerFactory();
        }
        return instance;
    }

    public void clearHolder() {
        getInstance().getHolder().clear();
    }
}
