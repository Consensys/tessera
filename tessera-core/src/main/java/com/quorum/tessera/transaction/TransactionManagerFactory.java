package com.quorum.tessera.transaction;

import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.exception.OperationCurrentlySuspended;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import java.lang.reflect.Proxy;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public interface TransactionManagerFactory {

    static TransactionManagerFactory newFactory() {
        return new DefaultTransactionManagerFactory();
    }

    TransactionManager create(
            EncryptedTransactionDAO encryptedTransactionDAO,
            Enclave enclave,
            EncryptedRawTransactionDAO encryptedRawTransactionDAO,
            ResendManager resendManager,
            PayloadPublisher payloadPublisher);

    class DefaultTransactionManagerFactory implements TransactionManagerFactory {

        private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTransactionManagerFactory.class);

        private final SyncState syncState;

        public DefaultTransactionManagerFactory(SyncState syncState) {
            this.syncState = syncState;
        }

        public DefaultTransactionManagerFactory() {
            this(SyncState.create());
        }

        @Override
        public TransactionManager create(
                EncryptedTransactionDAO encryptedTransactionDAO,
                Enclave enclave,
                EncryptedRawTransactionDAO encryptedRawTransactionDAO,
                ResendManager resendManager,
                PayloadPublisher payloadPublisher) {

            final TransactionManager transactionManager =
                    new TransactionManagerImpl(
                            encryptedTransactionDAO,
                            enclave,
                            encryptedRawTransactionDAO,
                            resendManager,
                            payloadPublisher);

            return (TransactionManager)
                    Proxy.newProxyInstance(
                            TransactionManagerFactory.class.getClassLoader(),
                            new Class[] {TransactionManager.class},
                            (proxy, method, args) -> {
                                LOGGER.debug("Enter : {} ", method.getName());
                                boolean isByPassFunction = method.getName().equals("storePayloadBypassResendMode");

                                if (!isByPassFunction && syncState.isResendMode()) {
                                    throw new OperationCurrentlySuspended("Operation is currently suspended.");
                                }
                                try {
                                    LOGGER.debug("Invoking : {}", method.getName());
                                    Object outcome = method.invoke(transactionManager, args);
                                    LOGGER.debug("Invoked : {} outcome: {}", method.getName(), outcome);
                                    return outcome;

                                } catch (java.lang.reflect.InvocationTargetException ex) {
                                    LOGGER.debug(null, ex);
                                    throw ex.getTargetException();
                                }
                            });
        }
    }
}
