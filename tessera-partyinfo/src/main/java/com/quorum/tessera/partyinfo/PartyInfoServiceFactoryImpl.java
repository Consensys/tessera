package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.service.locator.ServiceLocator;


public class PartyInfoServiceFactoryImpl implements PartyInfoServiceFactory {

    private final ServiceLocator serviceLocator = ServiceLocator.create();

    public <T> T find(final Class<T> type) {
        return serviceLocator.getServices().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Unable to find service type: " + type));
    }

    @Override
    public ResendManager resendManager() {
        return find(ResendManager.class);
    }

    @Override
    public PartyInfoService partyInfoService() {
        return find(PartyInfoService.class);
    }

    @Override
    public Enclave enclave() {
        return find(Enclave.class);
    }

    @Override
    public PayloadPublisher payloadPublisher() {
        return find(PayloadPublisher.class);
    }

    @Override
    public PartyInfoStore partyInfoStore() {
        return find(PartyInfoStore.class);
    }
}
