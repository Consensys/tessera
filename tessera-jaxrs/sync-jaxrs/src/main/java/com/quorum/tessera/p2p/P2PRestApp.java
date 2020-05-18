package com.quorum.tessera.p2p;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PartyInfoServiceFactory;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.recover.resend.BatchResendManager;
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

    private final PartyInfoService partyInfoService;

    private final PartyInfoParser partyInfoParser = PartyInfoParser.create();

    private final Enclave enclave;

    private final Config config;

    public P2PRestApp() {
        final ServiceFactory serviceFactory = ServiceFactory.create();
        this.config = serviceFactory.config();
        this.partyInfoService = PartyInfoServiceFactory.create(config).partyInfoService();
        this.enclave = EnclaveFactory.create().create(config);
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

        TransactionManager transactionManager = TransactionManager.create(config);
        BatchResendManager batchResendManager = BatchResendManager.create(config);

        final TransactionResource transactionResource = new TransactionResource(transactionManager);
        final RecoveryResource recoveryResource = new RecoveryResource(batchResendManager);

        if (runtimeContext.isRecoveryMode()) {
            return Set.of(partyInfoResource, iPWhitelistFilter, recoveryResource);
        }

        return Set.of(partyInfoResource, iPWhitelistFilter, transactionResource, recoveryResource);
    }

    @Override
    public AppType getAppType() {
        return AppType.P2P;
    }
}
