package com.quorum.tessera.privacygroup.publish;

import com.quorum.tessera.encryption.PublicKey;
import java.util.List;
import java.util.ServiceLoader;

public interface BatchPrivacyGroupPublisher {

  void publishPrivacyGroup(byte[] data, List<PublicKey> recipients);

  static BatchPrivacyGroupPublisher create() {
    return ServiceLoader.load(BatchPrivacyGroupPublisher.class).findFirst().get();
  }
}
