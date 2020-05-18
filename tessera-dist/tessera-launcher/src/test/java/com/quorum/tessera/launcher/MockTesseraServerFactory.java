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

    private static MockTesseraServerFactory INSTANCE = new MockTesseraServerFactory();

    public MockTesseraServerFactory() {
    }

    @Override
    public TesseraServer createServer(ServerConfig config, Set services) {
        return INSTANCE.create();
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }

    private TesseraServer create() {
        final TesseraServer mockServer = mock(TesseraServer.class);
        INSTANCE.getHolder().add(mockServer);
        return mockServer;
    }


    public List<TesseraServer> getHolder() {
        return getInstance().holder;
    }

    public static MockTesseraServerFactory getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new MockTesseraServerFactory();
        }
        return INSTANCE;
    }

    public void clearHolder() {
        getInstance().getHolder().clear();
    }
}
