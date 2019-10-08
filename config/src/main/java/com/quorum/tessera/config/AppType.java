package com.quorum.tessera.config;

import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public enum AppType {
    P2P(CommunicationType.REST, CommunicationType.GRPC, CommunicationType.WEB_SOCKET),

    Q2T(CommunicationType.REST, CommunicationType.GRPC),

    @XmlEnumValue("ThirdParty")
    THIRD_PARTY(CommunicationType.REST),

    ENCLAVE(CommunicationType.REST),

    ADMIN(CommunicationType.REST);

    private final Set<CommunicationType> allowedCommunicationTypes;

    AppType(CommunicationType... allowedCommunicationTypes) {
        this.allowedCommunicationTypes =
                Collections.unmodifiableSet(Arrays.stream(allowedCommunicationTypes).collect(Collectors.toSet()));
    }

    public Set<CommunicationType> getAllowedCommunicationTypes() {
        return allowedCommunicationTypes;
    }
}
