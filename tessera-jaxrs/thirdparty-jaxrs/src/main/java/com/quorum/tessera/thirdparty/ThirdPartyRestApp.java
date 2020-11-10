package com.quorum.tessera.thirdparty;

import com.quorum.tessera.api.common.RawTransactionResource;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.TransactionManagerFactory;
import io.swagger.annotations.Api;

import javax.ws.rs.ApplicationPath;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** The third party API */
@Api
@ApplicationPath("/")
public class ThirdPartyRestApp extends TesseraRestApplication implements com.quorum.tessera.config.apps.TesseraApp {

    private final Discovery discovery;

    private final TransactionManager transactionManager;

    public ThirdPartyRestApp() {
        this.discovery = Discovery.getInstance();
        this.transactionManager = TransactionManagerFactory.create().transactionManager().get();
    }

    @Override
    public Set<Object> getSingletons() {
        final RawTransactionResource rawTransactionResource = new RawTransactionResource(transactionManager);
        final PartyInfoResource partyInfoResource = new PartyInfoResource(discovery);
        final KeyResource keyResource = new KeyResource();

        return Stream.of(rawTransactionResource, partyInfoResource, keyResource).collect(Collectors.toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.THIRD_PARTY;
    }
}
