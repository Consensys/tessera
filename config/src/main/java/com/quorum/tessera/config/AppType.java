package com.quorum.tessera.config;

import com.quorum.tessera.config.apps.*;

import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singleton;

public enum AppType {
    P2P(P2PApp.class,
        new HashSet<>(Arrays.asList(CommunicationType.GRPC, CommunicationType.REST))),
    Q2T(Q2TApp.class,
        new HashSet<>(Arrays.asList(CommunicationType.GRPC, CommunicationType.REST))
        ),
    @XmlEnumValue("ThirdParty")
    THIRD_PARTY(ThirdPartyApp.class,
        singleton(CommunicationType.REST)),
    ENCLAVE(EnclaveApp.class,
        singleton(CommunicationType.REST)
    ),
    ADMIN(AdminApp.class,
        singleton(CommunicationType.REST)
    );

    private final Class<? extends TesseraApp> intf;
    private final Set<CommunicationType> allowedCommunicationTypes;

    AppType(Class<? extends TesseraApp> intf,
            Set<CommunicationType> allowedCommunicationTypes) {
        this.intf = intf;
        this.allowedCommunicationTypes = allowedCommunicationTypes;
    }

    public Class<? extends TesseraApp> getIntf() {
        return intf;
    }

    public Set<CommunicationType> getAllowedCommunicationTypes() {
        return allowedCommunicationTypes;
    }


}
