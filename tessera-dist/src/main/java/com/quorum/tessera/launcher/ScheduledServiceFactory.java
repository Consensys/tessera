package com.quorum.tessera.launcher;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.IntervalPropertyHelper;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.p2p.PartyInfoPoller;
import com.quorum.tessera.p2p.ResendPartyStore;
import com.quorum.tessera.p2p.ResendPartyStoreImpl;
import com.quorum.tessera.p2p.SyncPoller;
import com.quorum.tessera.partyinfo.*;
import com.quorum.tessera.service.ServiceContainer;
import com.quorum.tessera.threading.TesseraScheduledExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduledServiceFactory {

    private Config config;

    private List<TesseraScheduledExecutor> tesseraScheduledExecutors = new ArrayList<>();

    private ServiceContainer serviceContainer;

    private boolean enableSync = false;

    public ScheduledServiceFactory enableSync() {
        this.enableSync = true;
        return this;
    }

    private ScheduledServiceFactory(Config config) {
        this.config = config;
    }

    public static ScheduledServiceFactory fromConfig(Config config) {
        return new ScheduledServiceFactory(config);
    }

    public void build() {

        IntervalPropertyHelper intervalPropertyHelper = new IntervalPropertyHelper(config.getP2PServerConfig().getProperties());
        PartyInfoServiceFactory partyInfoServiceFactory = PartyInfoServiceFactory.create();


        PartyInfoService partyInfoService = partyInfoServiceFactory.partyInfoService()
            .orElseGet(() -> partyInfoServiceFactory.create(config));

        P2pClient p2pClient = P2pClientFactory.newFactory(config).create(config);

        if(enableSync) {

            ResendPartyStore resendPartyStore = new ResendPartyStoreImpl();
            TransactionRequester transactionRequester = TransactionRequesterFactory.newFactory().createTransactionRequester(config);
            SyncPoller syncPoller = new SyncPoller(resendPartyStore, transactionRequester, partyInfoService, p2pClient);
            ScheduledExecutorService scheduledExecutorService = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
            tesseraScheduledExecutors.add(new TesseraScheduledExecutor(scheduledExecutorService,syncPoller,intervalPropertyHelper.syncInterval(),5000L));
        }

        EnclaveKeySynchroniser enclaveKeySynchroniser = new EnclaveKeySynchroniser(partyInfoService);

        tesseraScheduledExecutors.add(new TesseraScheduledExecutor(java.util.concurrent.Executors.newSingleThreadScheduledExecutor(),enclaveKeySynchroniser,intervalPropertyHelper.enclaveKeySyncInterval(),5000L));

        PartyInfoPoller partyInfoPoller = new PartyInfoPoller(partyInfoService,p2pClient);

        tesseraScheduledExecutors.add(new TesseraScheduledExecutor(java.util.concurrent.Executors.newSingleThreadScheduledExecutor(),partyInfoPoller,intervalPropertyHelper.partyInfoInterval(),5000L));

        tesseraScheduledExecutors.forEach(TesseraScheduledExecutor::start);


        Enclave enclave = EnclaveFactory.create().create(config);
        serviceContainer = new ServiceContainer(enclave);
        serviceContainer.start();

    }

    public void stop() {
        try {
            tesseraScheduledExecutors.forEach(TesseraScheduledExecutor::stop);
        } finally {
            serviceContainer.stop();
        }
    }


}
