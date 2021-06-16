package com.quorum.tessera.transaction.publish;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.List;
import java.util.ServiceLoader;

public interface BatchPayloadPublisher {

  /**
   * Strips (leaving data intended only for that particular recipient) and publishes the payload to
   * each recipient identified by the provided keys.
   *
   * @param payload the payload object to be stripped and pushed
   * @param recipientKeys list of public keys identifying the target nodes
   */
  void publishPayload(EncodedPayload payload, List<PublicKey> recipientKeys);

  static BatchPayloadPublisher create() {
    return ServiceLoaderUtil.loadSingle(ServiceLoader.load(BatchPayloadPublisher.class));
  }
}
