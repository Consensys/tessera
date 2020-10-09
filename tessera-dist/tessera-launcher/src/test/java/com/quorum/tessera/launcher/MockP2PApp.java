package com.quorum.tessera.launcher;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.apps.TesseraApp;

public class MockP2PApp implements TesseraApp {
    @Override
    public CommunicationType getCommunicationType() {
        return CommunicationType.REST;
    }

    @Override
    public AppType getAppType() {
        return AppType.P2P;
    }
}
