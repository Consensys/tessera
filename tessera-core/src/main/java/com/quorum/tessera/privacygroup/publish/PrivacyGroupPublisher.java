package com.quorum.tessera.privacygroup.publish;

import com.quorum.tessera.encryption.PublicKey;
import java.util.ServiceLoader;

public interface PrivacyGroupPublisher {

  void publishPrivacyGroup(byte[] data, PublicKey recipients);

  static PrivacyGroupPublisher create() {
    return ServiceLoader.load(PrivacyGroupPublisher.class).findFirst().get();
  }
}
