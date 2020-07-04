package com.quorum.tessera.transaction;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PartyInfoServiceFactory;
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.transaction.resend.ResendManagerImpl;

public enum DefaultTransactionManagerFactory implements TransactionManagerFactory {

    INSTANCE;

    @Override
    public TransactionManager create(Config config) {

        PartyInfoServiceFactory partyInfoServiceFactory = PartyInfoServiceFactory.create();

        PartyInfoService partyInfoService = partyInfoServiceFactory.create(config);
        Enclave enclave = EnclaveFactory.create().create(config);
        EntityManagerDAOFactory entityManagerDAOFactory = EntityManagerDAOFactory.newFactory(config);
        EncryptedTransactionDAO encryptedTransactionDAO = entityManagerDAOFactory.createEncryptedTransactionDAO();
        EncryptedRawTransactionDAO encryptedRawTransactionDAO =
            entityManagerDAOFactory.createEncryptedRawTransactionDAO();


        ResendManager resendManager = new ResendManagerImpl(encryptedTransactionDAO, enclave);

        return new TransactionManagerImpl(
            encryptedTransactionDAO,
            enclave,
            encryptedRawTransactionDAO,
            resendManager,
            partyInfoService,
            100);
    }
}
