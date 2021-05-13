package com.quorum.tessera.privacygroup.publish;

import com.quorum.tessera.encryption.PublicKey;

public interface PrivacyGroupPublisher {

  void publishPrivacyGroup(byte[] data, PublicKey recipients);
}
