package com.quorum.tessera.p2p;

import static java.util.stream.Collectors.toSet;

import com.quorum.tessera.api.common.UpCheckResource;
import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.p2p.partyinfo.PartyInfoParser;
import com.quorum.tessera.p2p.partyinfo.PartyStore;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.recovery.workflow.LegacyResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import jakarta.ws.rs.ApplicationPath;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main application that is submitted to the HTTP server Contains all the service classes
 * created by the service locator
 */
@GlobalFilter
@ApplicationPath("/")
public class P2PRestApp extends TesseraRestApplication
    implements com.quorum.tessera.config.apps.TesseraApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(P2PRestApp.class);

  private final Discovery discovery;

  private final PartyInfoParser partyInfoParser = PartyInfoParser.create();

  private final Enclave enclave;

  private final PartyStore partyStore;

  private final TransactionManager transactionManager;

  private final BatchResendManager batchResendManager;

  private final LegacyResendManager legacyResendManager;

  private final PrivacyGroupManager privacyGroupManager;

  public P2PRestApp() {
    this(
        Discovery.create(),
        Enclave.create(),
        PartyStore.getInstance(),
        TransactionManager.create(),
        BatchResendManager.create(),
        LegacyResendManager.create(),
        PrivacyGroupManager.create());
  }

  public P2PRestApp(
      Discovery discovery,
      Enclave enclave,
      PartyStore partyStore,
      TransactionManager transactionManager,
      BatchResendManager batchResendManager,
      LegacyResendManager legacyResendManager,
      PrivacyGroupManager privacyGroupManager) {
    this.discovery = Objects.requireNonNull(discovery);
    this.enclave = Objects.requireNonNull(enclave);
    this.partyStore = Objects.requireNonNull(partyStore);
    this.transactionManager = Objects.requireNonNull(transactionManager);
    this.batchResendManager = Objects.requireNonNull(batchResendManager);
    this.legacyResendManager = Objects.requireNonNull(legacyResendManager);
    this.privacyGroupManager = Objects.requireNonNull(privacyGroupManager);
  }

  @Override
  public Set<Object> getSingletons() {

    RuntimeContext runtimeContext = RuntimeContext.getInstance();
    List<URI> peers = runtimeContext.getPeers();
    LOGGER.debug("Found configured peers {}", peers);

    peers.stream()
        .map(NodeUri::create)
        .map(NodeUri::asURI)
        .peek(u -> LOGGER.debug("Adding {} to party store", u))
        .forEach(partyStore::store);

    final PartyInfoResource partyInfoResource =
        new PartyInfoResource(
            discovery,
            partyInfoParser,
            runtimeContext.getP2pClient(),
            enclave,
            runtimeContext.isRemoteKeyValidation());

    final IPWhitelistFilter iPWhitelistFilter = new IPWhitelistFilter();

    final TransactionResource transactionResource =
        new TransactionResource(transactionManager, batchResendManager, legacyResendManager);

    final UpCheckResource upCheckResource = new UpCheckResource();

    final PrivacyGroupResource privacyGroupResource = new PrivacyGroupResource(privacyGroupManager);

    if (runtimeContext.isRecoveryMode()) {
      final RecoveryResource recoveryResource =
          new RecoveryResource(transactionManager, batchResendManager);
      return Set.of(partyInfoResource, iPWhitelistFilter, recoveryResource, upCheckResource);
    }
    return Set.of(
        partyInfoResource,
        iPWhitelistFilter,
        transactionResource,
        privacyGroupResource,
        upCheckResource);
  }

  @Override
  public Set<Class<?>> getClasses() {
    return Stream.concat(super.getClasses().stream(), Stream.of(P2PApiResource.class))
        .collect(toSet());
  }

  @Override
  public AppType getAppType() {
    return AppType.P2P;
  }
}
