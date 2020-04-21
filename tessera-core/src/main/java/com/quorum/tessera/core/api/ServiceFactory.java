package com.quorum.tessera.core.api;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import com.quorum.tessera.transaction.ResendManager;
import com.quorum.tessera.transaction.BatchResendManager;
import com.quorum.tessera.transaction.TransactionManager;

public interface ServiceFactory {

    PartyInfoService partyInfoService();

    Enclave enclave();

    TransactionManager transactionManager();

    EncryptedTransactionDAO encryptedTransactionDAO();

    EncryptedRawTransactionDAO encryptedRawTransactionDAO();

    ResendManager resendManager();

    BatchResendManager batchResendManager();

    PayloadPublisher payloadPublisher();

    ResendBatchPublisher batchPayloadPublisher();

    static ServiceFactory create() {
        return new ServiceFactoryImpl();
    }
}
