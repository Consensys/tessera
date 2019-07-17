package com.quorum.tessera.core.api;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import com.quorum.tessera.transaction.ResendManager;
import com.quorum.tessera.transaction.TransactionManager;

public interface ServiceFactory {

    PartyInfoService partyInfoService();

    Enclave enclave();

    TransactionManager transactionManager();

    Config config();

    ConfigService configService();

    EncryptedTransactionDAO encryptedTransactionDAO();

    EncryptedRawTransactionDAO encryptedRawTransactionDAO();

    ResendManager resendManager();

    PayloadPublisher payloadPublisher();

    static ServiceFactory create() {
        return new ServiceFactoryImpl();
    }
}
