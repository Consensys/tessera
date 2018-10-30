package com.quorum.tessera.config;

import com.quorum.tessera.config.apps.P2PApp;
import com.quorum.tessera.config.apps.Q2TApp;
import com.quorum.tessera.config.apps.TesseraApp;
import com.quorum.tessera.config.apps.ThirdPartyApp;

public enum AppType {
    P2P(P2PApp.class), Q2T(Q2TApp.class), ThirdParty(ThirdPartyApp.class);

    private Class<? extends TesseraApp> intf;

    AppType(Class<? extends TesseraApp> intf) {
        this.intf = intf;
    }

    public Class<? extends TesseraApp> getIntf() {
        return intf;
    }
}
