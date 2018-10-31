package com.quorum.tessera.config;

import com.quorum.tessera.config.apps.P2PApp;
import com.quorum.tessera.config.apps.Q2TApp;
import com.quorum.tessera.config.apps.TesseraApp;
import com.quorum.tessera.config.apps.ThirdPartyApp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum AppType {
    P2P(P2PApp.class,
        new HashSet<>(Arrays.asList(CommunicationType.GRPC,CommunicationType.REST)),
        new HashSet<>(Arrays.asList(InetServerSocket.class))),
    Q2T(Q2TApp.class,
        new HashSet<>(Arrays.asList(CommunicationType.REST)),
        new HashSet<>(Arrays.asList(InetServerSocket.class, UnixServerSocket.class))),
    ThirdParty(ThirdPartyApp.class,
        new HashSet<>(Arrays.asList(CommunicationType.REST)),
        new HashSet<>(Arrays.asList(InetServerSocket.class)));

    private final Class<? extends TesseraApp> intf;
    private final Set<CommunicationType> allowedCommunicationTypes;
    private final Set<Class<? extends ServerSocket>> allowedSocketTypes;

    AppType(Class<? extends TesseraApp> intf,
            Set<CommunicationType> allowedCommunicationTypes,
            Set<Class<? extends ServerSocket>> allowedSocketTypes) {
        this.intf = intf;
        this.allowedCommunicationTypes = allowedCommunicationTypes;
        this.allowedSocketTypes = allowedSocketTypes;
    }

    public Class<? extends TesseraApp> getIntf() {
        return intf;
    }

    public Set<CommunicationType> getAllowedCommunicationTypes() {
        return allowedCommunicationTypes;
    }

    public Set<Class<? extends ServerSocket>> getAllowedSocketTypes() {
        return allowedSocketTypes;
    }
}
