package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.Enclave;
import java.util.Objects;

public class PartyInfoServiceFactoryImpl implements PartyInfoServiceFactory {

    private final PartyInfoService partyInfoService;

    private final Enclave enclave;

    public PartyInfoServiceFactoryImpl(PartyInfoService partyInfoService,
                                       Enclave enclave) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.enclave = Objects.requireNonNull(enclave);
    }

    @Override
    public PartyInfoService partyInfoService() {
        return partyInfoService;
    }

    @Override
    public Enclave enclave() {
        return enclave;
    }
}
