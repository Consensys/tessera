package com.github.tessera.node;

import java.util.Collection;

public interface TransactionRequester {

    void requestAllTransactionsFromNode(Collection<String> uris);

}
