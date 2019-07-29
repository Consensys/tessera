package com.quorum.tessera.config.apps;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;

public class OtherMockTesseraApp implements TesseraApp {

    @Override
    public CommunicationType getCommunicationType() {
        return CommunicationType.WEB_SOCKET;
    }

    @Override
    public AppType getAppType() {
        return AppType.THIRD_PARTY;
    }
}
