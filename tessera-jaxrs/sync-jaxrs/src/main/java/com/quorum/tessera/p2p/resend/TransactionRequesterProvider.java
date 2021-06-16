package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.enclave.Enclave;

public class TransactionRequesterProvider {

  public static TransactionRequester provider() {
    Enclave enclave = Enclave.create();
    ResendClient resendClient = ResendClient.create();
    return new TransactionRequesterImpl(enclave, resendClient);
  }
}
