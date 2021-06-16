package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;

public class BatchTransactionRequesterProvider {

  public static BatchTransactionRequester provider() {
    final Enclave enclave = Enclave.create();
    final RecoveryClient client = RecoveryClient.create();

    return new RestBatchTransactionRequester(enclave, client, 100);
  }
}
