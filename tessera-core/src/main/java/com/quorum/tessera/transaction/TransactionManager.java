package com.quorum.tessera.transaction;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PartyInfoServiceFactory;
import com.quorum.tessera.partyinfo.ResendResponse;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.api.model.*;
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.transaction.resend.ResendManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface TransactionManager {

    SendResponse send(SendRequest sendRequest);

    SendResponse sendSignedTransaction(SendSignedRequest sendRequest);

    void delete(DeleteRequest request);

    ResendResponse resend(ResendRequest request);

    MessageHash storePayload(byte[] toByteArray);

    ReceiveResponse receive(ReceiveRequest request);

    StoreRawResponse store(StoreRawRequest storeRequest);

    boolean isSender(String ptmHash);

    List<PublicKey> getParticipants(String ptmHash);

    Logger LOGGER = LoggerFactory.getLogger(TransactionManager.class);

    static TransactionManager create(Config config) {
        LOGGER.debug("Creating TransactionManager with {}", config);

        return ServiceLoaderUtil.load(TransactionManager.class).orElseGet(() -> {
            PartyInfoService partyInfoService = PartyInfoServiceFactory.create(config).partyInfoService();
            Enclave enclave = EnclaveFactory.create().create(config);
            EntityManagerDAOFactory entityManagerDAOFactory = EntityManagerDAOFactory.newFactory(config);
            EncryptedTransactionDAO encryptedTransactionDAO = entityManagerDAOFactory.createEncryptedTransactionDAO();
            EncryptedRawTransactionDAO encryptedRawTransactionDAO =
                entityManagerDAOFactory.createEncryptedRawTransactionDAO();

            ResendManager resendManager = new ResendManagerImpl(encryptedTransactionDAO, enclave);
            PrivacyHelper privacyHelper = new PrivacyHelperImpl(encryptedTransactionDAO);

            return new TransactionManagerImpl(
                encryptedTransactionDAO,
                enclave,
                encryptedRawTransactionDAO,
                resendManager,
                partyInfoService,
                privacyHelper,
                100);
        });

    }
}
