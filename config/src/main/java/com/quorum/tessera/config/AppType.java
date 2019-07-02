package com.quorum.tessera.config;

import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public enum AppType {
    P2P(CommunicationType.GRPC, CommunicationType.REST),

    Q2T(CommunicationType.GRPC, CommunicationType.REST),

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
