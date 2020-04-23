package com.quorum.tessera.recover;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.partyinfo.*;

import java.net.URI;
//TODO: MOve this out into party info module and kill off spring nonesense
public class PartyInfoServiceFactory implements com.quorum.tessera.partyinfo.PartyInfoServiceFactory {

    private Config config;

    public PartyInfoServiceFactory(Config config) {
        this.config = config;
    }

    static PartyInfoService create(Config config) {


        PartyInfoServiceFactory partyInfoServiceFactory = new PartyInfoServiceFactory(config);


        return partyInfoServiceFactory.partyInfoService();
    }


    @Override
    public PartyInfoService partyInfoService() {
        return new PartyInfoServiceImpl(this);
    }

    @Override
    public Enclave enclave() {
        return EnclaveFactory.create().create(config);
    }

    @Override
    public PayloadPublisher payloadPublisher() {
        return PayloadPublisherFactory.newFactory(config)
            .create(config);
    }

    @Override
    public ResendBatchPublisher resendBatchPublisher() {
        return ResendBatchPublisherFactory.newFactory(config).create(config);
    }

    @Override
    public PartyInfoStore partyInfoStore() {
        URI uri = config.getP2PServerConfig().getServerUri();
        return new PartyInfoStore(uri);
    }
}
