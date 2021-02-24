package com.quorum.tessera.q2t;

import com.quorum.tessera.api.common.RawTransactionResource;
import com.quorum.tessera.api.common.UpCheckResource;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.TransactionManagerFactory;

import javax.ws.rs.ApplicationPath;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * The main application that is submitted to the HTTP server Contains all the service classes created by the service
 * locator
 */
@ApplicationPath("/")
public class Q2TRestApp extends TesseraRestApplication {

    private final ServiceLocator serviceLocator;

    public Q2TRestApp() {
        this(ServiceLocator.create());
    }

    public Q2TRestApp(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    public Set<Object> getSingletons() {

        Config config =
                serviceLocator.getServices().stream()
                        .filter(Config.class::isInstance)
                        .map(Config.class::cast)
                        .findAny()
                        .get();

        TransactionManager transactionManager = TransactionManagerFactory.create().create(config);
        EncodedPayloadManager encodedPayloadManager = EncodedPayloadManager.create(config);
        final PrivacyGroupManager privacyGroupManager = PrivacyGroupManager.create(config);

        TransactionResource transactionResource = new TransactionResource(transactionManager, privacyGroupManager);
        TransactionResource3 transactionResource3 = new TransactionResource3(transactionManager, privacyGroupManager);

        RawTransactionResource rawTransactionResource = new RawTransactionResource(transactionManager);
        EncodedPayloadResource encodedPayloadResource =
                new EncodedPayloadResource(encodedPayloadManager, transactionManager);
        final UpCheckResource upCheckResource = new UpCheckResource(transactionManager);

        final PrivacyGroupResource privacyGroupResource = new PrivacyGroupResource(privacyGroupManager);

        if (config.getClientMode() == ClientMode.ORION) {
            final BesuTransactionResource besuResource =
                    new BesuTransactionResource(transactionManager, privacyGroupManager);
            return Set.of(besuResource, rawTransactionResource, privacyGroupResource, upCheckResource);
        }

        return Set.of(
                transactionResource,
                rawTransactionResource,
                encodedPayloadResource,
                privacyGroupResource,
                upCheckResource,
                transactionResource3);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Stream.concat(super.getClasses().stream(), Stream.of(Q2TApiResource.class)).collect(toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.Q2T;
    }
}
