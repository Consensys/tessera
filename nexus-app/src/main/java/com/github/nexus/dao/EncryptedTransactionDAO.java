package com.github.nexus.dao;

import com.github.nexus.entity.EncryptedTransaction;

import java.util.List;

public interface EncryptedTransactionDAO {

    EncryptedTransaction save(EncryptedTransaction entity);

    List<EncryptedTransaction> retrieveAllTransactions();

}
