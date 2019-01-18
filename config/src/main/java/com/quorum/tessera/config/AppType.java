package com.quorum.tessera.config;

import com.quorum.tessera.config.apps.*;

import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singleton;

public enum AppType {
    P2P(P2PApp.class,
        new HashSet<>(Arrays.asList(CommunicationType.GRPC, CommunicationType.REST)),
        singleton(InetServerSocket.class)),
    Q2T(Q2TApp.class,
        // TODO UNIX_SOCKET will be removed when we will have a netty server configurable for both unix/inet sockets
        new HashSet<>(Arrays.asList(CommunicationType.GRPC, CommunicationType.REST, CommunicationType.UNIX_SOCKET)),
        new HashSet<>(Arrays.asList(InetServerSocket.class, UnixServerSocket.class))),
    @XmlEnumValue("ThirdParty")
    THIRD_PARTY(ThirdPartyApp.class,
        singleton(CommunicationType.REST),
        singleton(InetServerSocket.class)),
    ENCLAVE(EnclaveApp.class,
        singleton(CommunicationType.REST),
        singleton(InetServerSocket.class)
    ),
    ADMIN(AdminApp.class,
        singleton(CommunicationType.REST),
        singleton(InetServerSocket.class)
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
