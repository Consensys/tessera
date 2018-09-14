package com.quorum.tessera.sync;

/**
 * Makes requests to other nodes to resend their transactions
 * <p>
 * Handles creating the correct entity and retrying on a failed attempt
 */
public interface TransactionRequester {

    int MAX_ATTEMPTS = 5;

    /**
     * Makes a request to the given node to resend transactions for
     *
     * @param url the URL to contact for resending
     * @return whether all the resend requests for all keys was successful or not
     */
    boolean requestAllTransactionsFromNode(String url);

}
