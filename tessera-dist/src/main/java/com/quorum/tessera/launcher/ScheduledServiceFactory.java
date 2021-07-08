package com.quorum.tessera.launcher;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.IntervalPropertyHelper;
import com.quorum.tessera.discovery.EnclaveKeySynchroniser;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.p2p.partyinfo.PartyInfoBroadcaster;
import com.quorum.tessera.p2p.resend.ResendPartyStore;
import com.quorum.tessera.p2p.resend.SyncPoller;
import com.quorum.tessera.p2p.resend.TransactionRequester;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.service.ServiceContainer;
import com.quorum.tessera.threading.TesseraScheduledExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledServiceFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledServiceFactory.class);

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

    IntervalPropertyHelper intervalPropertyHelper =
        new IntervalPropertyHelper(config.getP2PServerConfig().getProperties());
    LOGGER.info("Creating p2p client");
    P2pClient p2pClient = P2pClient.create();
    LOGGER.info("Created p2p client {}", p2pClient);

    if (enableSync) {

      ResendPartyStore resendPartyStore = ResendPartyStore.create();
      TransactionRequester transactionRequester = TransactionRequester.create();
      SyncPoller syncPoller = new SyncPoller(resendPartyStore, transactionRequester, p2pClient);
      ScheduledExecutorService scheduledExecutorService =
          java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
      tesseraScheduledExecutors.add(
          new TesseraScheduledExecutor(
              scheduledExecutorService, syncPoller, intervalPropertyHelper.syncInterval(), 5000L));
    }

    LOGGER.info("Creating EnclaveKeySynchroniser");
    final EnclaveKeySynchroniser enclaveKeySynchroniser =
        ServiceLoader.load(EnclaveKeySynchroniser.class).stream()
            .map(ServiceLoader.Provider::get)
            .findFirst()
            .get();

    LOGGER.info("Created EnclaveKeySynchroniser {}", enclaveKeySynchroniser);

    tesseraScheduledExecutors.add(
        new TesseraScheduledExecutor(
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor(),
            () -> enclaveKeySynchroniser.syncKeys(),
            intervalPropertyHelper.enclaveKeySyncInterval(),
            5000L));

    LOGGER.info("Creating PartyInfoBroadcaster");

    PartyInfoBroadcaster partyInfoPoller = new PartyInfoBroadcaster(p2pClient);
    LOGGER.info("Created PartyInfoBroadcaster {}", partyInfoPoller);

    tesseraScheduledExecutors.add(
        new TesseraScheduledExecutor(
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor(),
            partyInfoPoller,
            intervalPropertyHelper.partyInfoInterval(),
            5000L));

    tesseraScheduledExecutors.forEach(TesseraScheduledExecutor::start);

    LOGGER.info("Creating Enclave");

    Enclave enclave = Enclave.create();
    LOGGER.info("Created Enclave {}", enclave);

    serviceContainer = new ServiceContainer(enclave);
    LOGGER.info("Starting Enclave");

    serviceContainer.start();

    LOGGER.info("Started Enclave");
  }

  public void stop() {
    try {
      tesseraScheduledExecutors.forEach(TesseraScheduledExecutor::stop);
    } finally {
      serviceContainer.stop();
    }
  }
}
