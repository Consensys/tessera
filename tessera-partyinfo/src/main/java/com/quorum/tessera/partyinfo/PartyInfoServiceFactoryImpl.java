package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class PartyInfoServiceFactoryImpl implements PartyInfoServiceFactory {

    private static final AtomicReference<PartyInfoService> REF = new AtomicReference<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoServiceFactoryImpl.class);

    @Override
    public PartyInfoService create(Config config) {
        LOGGER.info("Enter create [{},{}]",config,this);

        if (REF.get() == null) {

            LOGGER.info("Create party info service from {} . Factory {}",config,this);

            Enclave enclave = EnclaveFactory.create().create(config);
            PayloadPublisher payloadPublisher =
                PayloadPublisherFactory.newFactory(config).create(config);
            PartyInfoStore partyInfoStore =
                PartyInfoStore.create(
                    config.getP2PServerConfig().getServerUri());
            KnownPeerCheckerFactory knownPeerCheckerFactory =
                new KnownPeerCheckerFactory();


            PartyInfoService partyInfoService = new PartyInfoServiceImpl(
                partyInfoStore,
                enclave,
                payloadPublisher,
                knownPeerCheckerFactory);
            REF.set(partyInfoService);

        } else {
            LOGGER.info("Looked up existing [{},{}]",config,this);
        }



        return REF.get();

    }

}
