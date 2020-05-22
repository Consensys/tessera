package com.quorum.tessera.partyinfo;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public interface PartyInfoServiceFactory {

    PartyInfoService partyInfoService();

    AtomicReference<PartyInfoServiceFactory> FACTORY_ATOMIC_REFERENCE = new AtomicReference<>();

    static PartyInfoServiceFactory create(Config config) {

        final PartyInfoServiceFactory partyInfoServiceFactory = Optional.ofNullable(FACTORY_ATOMIC_REFERENCE.get())
            .orElse(ServiceLoaderUtil.load(PartyInfoServiceFactory.class)
                .orElseGet(() -> {
                    Enclave enclave = EnclaveFactory.create().create(config);
                    PayloadPublisher payloadPublisher = PayloadPublisherFactory.newFactory(config).create(config);
                    ResendBatchPublisher resendBatchPublisher = ResendBatchPublisherFactory.newFactory(config).create(config);

                    PartyInfoStore partyInfoStore = PartyInfoStore.create(config.getP2PServerConfig().getServerUri());
                    PartyInfoService partyInfoService = new PartyInfoServiceImpl(partyInfoStore,enclave,payloadPublisher,resendBatchPublisher);

                    return new PartyInfoServiceFactoryImpl(partyInfoService);
                }));

        FACTORY_ATOMIC_REFERENCE.set(partyInfoServiceFactory);

        return partyInfoServiceFactory;

    }
}
