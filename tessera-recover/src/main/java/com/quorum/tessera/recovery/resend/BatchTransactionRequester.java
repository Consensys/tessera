package com.quorum.tessera.recovery.resend;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;

import java.util.ServiceLoader;

/**
 * Makes requests to other nodes to resend their transactions
 *
 * <p>Handles creating the correct entity and retrying on a failed attempt
 */
public interface BatchTransactionRequester {

    int MAX_ATTEMPTS = 5;

    /**
     * Makes a request to the given node to resend transactions for
     *
     * @param url the URL to contact for resending
     * @return whether all the resend requests for all keys was successful or not
     */
    boolean requestAllTransactionsFromNode(String url);

    /**
     * Makes a request to the given node that run on a legacy version to resend transactions
     *
     * @param url
     * @return
     */
    boolean requestAllTransactionsFromLegacyNode(String url);

    CommunicationType communicationType();

    static BatchTransactionRequester create() {
        Config config = ConfigFactory.create().getConfig();
        CommunicationType communicationType = config.getP2PServerConfig().getCommunicationType();
        return ServiceLoader.load(BatchTransactionRequester.class).stream()
            .map(ServiceLoader.Provider::get)
            .filter(s -> s.communicationType() == communicationType)
            .findFirst()
            .get();

    }

}
