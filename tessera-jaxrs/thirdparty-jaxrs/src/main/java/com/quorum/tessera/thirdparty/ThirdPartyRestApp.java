package com.quorum.tessera.thirdparty;

import com.quorum.tessera.api.common.RawTransactionResource;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.annotations.Api;

import javax.ws.rs.ApplicationPath;
import java.util.Set;

/** The third party API */
@Api
@ApplicationPath("/")
public class ThirdPartyRestApp extends TesseraRestApplication implements com.quorum.tessera.config.apps.TesseraApp {

    private final Discovery discovery;

    private final TransactionManager transactionManager;

    public ThirdPartyRestApp() {
        this(Discovery.create(),TransactionManager.create());
    }

    protected ThirdPartyRestApp(Discovery discovery, TransactionManager transactionManager) {
        this.discovery = discovery;
        this.transactionManager = transactionManager;
    }

    @Override
    public Set<Object> getSingletons() {
        final RawTransactionResource rawTransactionResource = new RawTransactionResource(transactionManager);
        final PartyInfoResource partyInfoResource = new PartyInfoResource(discovery);
        final KeyResource keyResource = new KeyResource();

        return Set.of(rawTransactionResource, partyInfoResource, keyResource);
    }

    @Override
    public AppType getAppType() {
        return AppType.THIRD_PARTY;
    }
}
