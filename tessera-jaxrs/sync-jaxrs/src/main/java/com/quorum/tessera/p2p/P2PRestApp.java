package com.quorum.tessera.p2p;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.TransactionManagerFactory;
import io.swagger.annotations.Api;

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

    private final Discovery partyInfoService;

    private final PartyInfoParser partyInfoParser = PartyInfoParser.create();

    private final Enclave enclave;

    private final Config config;

    public P2PRestApp() {
        final ServiceFactory serviceFactory = ServiceFactory.create();
        this.config = serviceFactory.config();
        this.enclave = EnclaveFactory.create().create(config);
        this.partyInfoService = Discovery.getInstance();
    }

    @Override
    public Set<Object> getSingletons() {

        RuntimeContext runtimeContext = RuntimeContext.getInstance();

        final PartyInfoResource partyInfoResource =
                new PartyInfoResource(
                        partyInfoService,
                        partyInfoParser,
                        runtimeContext.getP2pClient(),
                        enclave,
                        runtimeContext.isRemoteKeyValidation());

        final IPWhitelistFilter iPWhitelistFilter = new IPWhitelistFilter();

        TransactionManagerFactory transactionManagerFactory = TransactionManagerFactory.create();
        TransactionManager transactionManager = transactionManagerFactory.create(config);
        PayloadEncoder payloadEncoder = PayloadEncoder.create();

        final TransactionResource transactionResource = new TransactionResource(transactionManager, payloadEncoder);

        return Set.of(partyInfoResource, iPWhitelistFilter, transactionResource);
    }

    @Override
    public AppType getAppType() {
        return AppType.P2P;
    }
}
