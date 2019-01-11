package com.quorum.tessera.config;

import com.quorum.tessera.config.apps.EnclaveApp;
import com.quorum.tessera.config.apps.P2PApp;
import com.quorum.tessera.config.apps.Q2TApp;
import com.quorum.tessera.config.apps.TesseraApp;
import com.quorum.tessera.config.apps.ThirdPartyApp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlEnumValue;

public enum AppType {
    P2P(P2PApp.class,
        new HashSet<>(Arrays.asList(CommunicationType.GRPC,CommunicationType.REST)),
        new HashSet<>(Arrays.asList(InetServerSocket.class))),
    Q2T(Q2TApp.class,
        // TODO UNIX_SOCKET will be removed when we will have a netty server configurable for both unix/inet sockets
        new HashSet<>(Arrays.asList(CommunicationType.GRPC,CommunicationType.REST,CommunicationType.UNIX_SOCKET)),
        new HashSet<>(Arrays.asList(InetServerSocket.class, UnixServerSocket.class))),
    @XmlEnumValue("ThirdParty")
    THIRD_PARTY(ThirdPartyApp.class,
        new HashSet<>(Arrays.asList(CommunicationType.REST)),
        new HashSet<>(Arrays.asList(InetServerSocket.class))),
    ENCLAVE(EnclaveApp.class,
            Collections.singleton(CommunicationType.REST),
            Collections.singleton(InetServerSocket.class)
    );

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
