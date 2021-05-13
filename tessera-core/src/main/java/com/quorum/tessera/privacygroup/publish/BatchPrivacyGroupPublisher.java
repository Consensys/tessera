package com.quorum.tessera.privacygroup.publish;

import com.quorum.tessera.encryption.PublicKey;
import java.util.List;

public interface BatchPrivacyGroupPublisher {

  void publishPrivacyGroup(byte[] data, List<PublicKey> recipients);
}
