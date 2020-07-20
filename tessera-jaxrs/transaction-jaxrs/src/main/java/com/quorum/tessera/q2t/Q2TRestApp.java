package com.quorum.tessera.q2t;

import com.quorum.tessera.api.common.RawTransactionResource;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.TransactionManagerFactory;
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
@ApplicationPath("/")
public class Q2TRestApp extends TesseraRestApplication {

    public Q2TRestApp() {
    }

    @Override
    public Set<Object> getSingletons() {

        TransactionManager transactionManager = TransactionManagerFactory.create().transactionManager().get();
        TransactionResource transactionResource = new TransactionResource(transactionManager);
        RawTransactionResource rawTransactionResource = new RawTransactionResource(transactionManager);

        return Set.of(transactionResource, rawTransactionResource);
    }

    @Override
    public AppType getAppType() {
        return AppType.Q2T;
    }
}
