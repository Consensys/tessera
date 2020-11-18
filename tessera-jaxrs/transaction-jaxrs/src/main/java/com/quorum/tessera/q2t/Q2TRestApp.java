package com.quorum.tessera.q2t;

import com.quorum.tessera.api.common.RawTransactionResource;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.annotations.Api;

import javax.ws.rs.ApplicationPath;
import java.util.Objects;
import java.util.Set;

/**
 * The main application that is submitted to the HTTP server Contains all the service classes created by the service
 * locator
 */
@Api
@ApplicationPath("/")
public class Q2TRestApp extends TesseraRestApplication implements com.quorum.tessera.config.apps.TesseraApp {

    private TransactionManager transactionManager;

    private EncodedPayloadManager encodedPayloadManager;

    protected Q2TRestApp(TransactionManager transactionManager, EncodedPayloadManager encodedPayloadManager) {
        this.transactionManager = Objects.requireNonNull(transactionManager);
        this.encodedPayloadManager = Objects.requireNonNull(encodedPayloadManager);
    }

    public Q2TRestApp() {
        this.transactionManager = TransactionManager.create();
        this.encodedPayloadManager = EncodedPayloadManager.create();
    }

    @Override
    public Set<Object> getSingletons() {

        TransactionResource transactionResource = new TransactionResource(transactionManager);
        RawTransactionResource rawTransactionResource = new RawTransactionResource(transactionManager);
        EncodedPayloadResource encodedPayloadResource
            = new EncodedPayloadResource(encodedPayloadManager, transactionManager);

        return Set.of(transactionResource, rawTransactionResource, encodedPayloadResource);
    }

    @Override
    public AppType getAppType() {
        return AppType.Q2T;
    }
}
