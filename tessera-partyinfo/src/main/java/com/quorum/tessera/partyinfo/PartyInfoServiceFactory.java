package com.quorum.tessera.partyinfo;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public interface PartyInfoServiceFactory {

    PartyInfoService partyInfoService();

    Enclave enclave();

    AtomicReference<PartyInfoServiceFactory> FACTORY_ATOMIC_REFERENCE = new AtomicReference<>();

    static PartyInfoServiceFactory create(Config config) {

        if(FACTORY_ATOMIC_REFERENCE.get() != null) {
            return FACTORY_ATOMIC_REFERENCE.get();
        }
        Optional<PartyInfoServiceFactory> optionalPartyInfoServiceFactory = ServiceLoaderUtil.load(PartyInfoServiceFactory.class);
        if(optionalPartyInfoServiceFactory.isPresent()) {
            PartyInfoServiceFactory partyInfoServiceFactory = optionalPartyInfoServiceFactory.get();
            FACTORY_ATOMIC_REFERENCE.set(partyInfoServiceFactory);
            return partyInfoServiceFactory;
        }

        Enclave enclave = EnclaveFactory.create().create(config);
        PayloadPublisher payloadPublisher = PayloadPublisherFactory.newFactory(config).create(config);
        ResendBatchPublisher resendBatchPublisher = ResendBatchPublisherFactory.newFactory(config).create(config);
        URI uri = config.getP2PServerConfig().getServerUri();
        PartyInfoStore partyInfoStore = new PartyInfoStore(uri);
        PartyInfoService partyInfoService = new PartyInfoServiceImpl(partyInfoStore,enclave,payloadPublisher,resendBatchPublisher);

        PartyInfoServiceFactory partyInfoServiceFactory = new PartyInfoServiceFactoryImpl(partyInfoService,enclave);
        FACTORY_ATOMIC_REFERENCE.set(partyInfoServiceFactory);
        return partyInfoServiceFactory;
    }
}
