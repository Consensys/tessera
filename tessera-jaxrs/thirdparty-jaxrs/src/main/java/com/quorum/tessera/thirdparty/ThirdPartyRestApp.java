package com.quorum.tessera.thirdparty;

import com.quorum.tessera.api.common.RawTransactionResource;
import com.quorum.tessera.api.common.UpCheckResource;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.TransactionManagerFactory;

import javax.ws.rs.ApplicationPath;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/** The third party API */
@ApplicationPath("/")
public class ThirdPartyRestApp extends TesseraRestApplication {

    private final Discovery discovery;

    private final TransactionManager transactionManager;

    public ThirdPartyRestApp() {
        final ServiceFactory serviceFactory = ServiceFactory.create();

        Config config = serviceFactory.config();
        this.discovery = Discovery.getInstance();
        this.transactionManager = TransactionManagerFactory.create().create(config);
    }

    @Override
    public Set<Object> getSingletons() {
        final RawTransactionResource rawTransactionResource = new RawTransactionResource(transactionManager);
        final PartyInfoResource partyInfoResource = new PartyInfoResource(discovery);
        final KeyResource keyResource = new KeyResource();
        final UpCheckResource upCheckResource = new UpCheckResource(transactionManager);

        return Stream.of(rawTransactionResource, partyInfoResource, keyResource, upCheckResource)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Stream.concat(super.getClasses().stream(), Stream.of(ThirdPartyApiResource.class))
            .collect(toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.THIRD_PARTY;
    }
}
