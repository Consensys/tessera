package com.github.tessera.sync;

public interface TransactionRequester {

    int MAX_ATTEMPTS = 5;

    boolean requestAllTransactionsFromNode(String uri);

}
