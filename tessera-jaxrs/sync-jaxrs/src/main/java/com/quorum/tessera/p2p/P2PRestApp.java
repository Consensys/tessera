package com.quorum.tessera.p2p;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.p2p.partyinfo.PartyInfoParser;
import com.quorum.tessera.p2p.partyinfo.PartyStore;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.TransactionManagerFactory;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;
import java.util.Set;

/**
 * The main application that is submitted to the HTTP server Contains all the service classes created by the service
 * locator
 */
@Api
@GlobalFilter
@ApplicationPath("/")
public class P2PRestApp extends TesseraRestApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(P2PRestApp.class);

    private final Discovery discovery;

    private final PartyInfoParser partyInfoParser = PartyInfoParser.create();

    private final Enclave enclave;

    private final PartyStore partyStore;

    public P2PRestApp() {
        this.discovery = Discovery.getInstance();
        this.enclave = EnclaveFactory.create().enclave().get();
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

        TransactionManagerFactory transactionManagerFactory = TransactionManagerFactory.create();
        TransactionManager transactionManager = transactionManagerFactory.transactionManager().get();
        PayloadEncoder payloadEncoder = PayloadEncoder.create();

        final TransactionResource transactionResource = new TransactionResource(transactionManager,payloadEncoder);

        return Set.of(partyInfoResource, iPWhitelistFilter, transactionResource);
    }

    @Override
    public AppType getAppType() {
        return AppType.P2P;
    }
}
