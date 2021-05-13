package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import com.quorum.tessera.transaction.ReceiveResponse;
import com.quorum.tessera.transaction.SendRequest;

public class MockEncodedPayloadManager implements EncodedPayloadManager {

  @Override
  public EncodedPayload create(final SendRequest request) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ReceiveResponse decrypt(
      final EncodedPayload payload, final PublicKey maybeDefaultRecipient) {
    throw new UnsupportedOperationException();
  }
}
