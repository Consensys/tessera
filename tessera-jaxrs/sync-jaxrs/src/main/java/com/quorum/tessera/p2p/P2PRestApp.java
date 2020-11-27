package com.quorum.tessera.p2p;

import com.quorum.tessera.api.common.UpCheckResource;
import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.p2p.partyinfo.PartyInfoParser;
import com.quorum.tessera.p2p.partyinfo.PartyStore;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.recovery.workflow.LegacyResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.TransactionManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * The main application that is submitted to the HTTP server Contains all the service classes created by the service
 * locator
 */
@GlobalFilter
@ApplicationPath("/")
public class P2PRestApp extends TesseraRestApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(P2PRestApp.class);

    private final Discovery discovery;

    private final PartyInfoParser partyInfoParser = PartyInfoParser.create();

    private final Enclave enclave;

    private final Config config;

    private final PartyStore partyStore;

    public P2PRestApp() {
        this.config = ServiceFactory.create().config();
        this.enclave = EnclaveFactory.create().create(config);
        this.discovery = Discovery.getInstance();
        this.partyStore = PartyStore.getInstance();
    }

    @Override
    public Set<Object> getSingletons() {
        RuntimeContext runtimeContext = RuntimeContext.getInstance();
        LOGGER.debug("Found configured peers {}", runtimeContext.getPeers());

        runtimeContext.getPeers().stream()
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

        TransactionManager transactionManager = TransactionManagerFactory.create().create(config);
        BatchResendManager batchResendManager = BatchResendManager.create(config);
        PayloadEncoder payloadEncoder = PayloadEncoder.create();
        final LegacyResendManager legacyResendManager = LegacyResendManager.create(config);

        final TransactionResource transactionResource =
            new TransactionResource(transactionManager, batchResendManager, payloadEncoder, legacyResendManager);
        final RecoveryResource recoveryResource =
            new RecoveryResource(transactionManager, batchResendManager, payloadEncoder);
        final UpCheckResource upCheckResource = new UpCheckResource(transactionManager);

        final PrivacyGroupManager privacyGroupManager = PrivacyGroupManager.create(config);
        final PrivacyGroupResource privacyGroupResource = new PrivacyGroupResource(privacyGroupManager);

        if (runtimeContext.isRecoveryMode()) {
            return Set.of(partyInfoResource, iPWhitelistFilter, recoveryResource, upCheckResource);
        }
        return Set.of(partyInfoResource, iPWhitelistFilter, transactionResource, privacyGroupResource, upCheckResource);
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
