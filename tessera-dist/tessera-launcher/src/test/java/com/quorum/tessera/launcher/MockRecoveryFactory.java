package com.quorum.tessera.launcher;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.recovery.Recovery;
import com.quorum.tessera.recovery.RecoveryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockRecoveryFactory implements RecoveryFactory {

    private final List<Recovery> holder = new ArrayList<>();

    public static MockRecoveryFactory instance = new MockRecoveryFactory();

    public MockRecoveryFactory() {}

    @Override
    public Recovery create(Config config) {
        return instance.create();
    }

    private Recovery create() {
        Recovery mockRecoveryManager = mock(Recovery.class);
        when(mockRecoveryManager.recover()).thenThrow(RuntimeException.class);
        instance.getHolder().add(mockRecoveryManager);
        return mockRecoveryManager;
    }

    public List<Recovery> getHolder() {
        return getInstance().holder;
    }

    public static MockRecoveryFactory getInstance() {
        if (instance == null) {
            instance = new MockRecoveryFactory();
        }
        return instance;
    }
}
