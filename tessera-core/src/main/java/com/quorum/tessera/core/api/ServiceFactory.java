package com.quorum.tessera.core.api;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.transaction.resend.batch.BatchResendManager;

public interface ServiceFactory {

    Enclave enclave();

    TransactionManager transactionManager();

    EncryptedTransactionDAO encryptedTransactionDAO();

    EncryptedRawTransactionDAO encryptedRawTransactionDAO();

    ResendManager resendManager();

    BatchResendManager batchResendManager();

    Config config();

    static ServiceFactory create() {
        return ServiceLoaderUtil.load(ServiceFactory.class)
            .orElse(new ServiceFactoryImpl());
    }
}
