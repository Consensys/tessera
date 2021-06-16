package com.quorum.tessera.privacygroup.internal;

import com.quorum.tessera.data.PrivacyGroupDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher;

public class PrivacyGroupManagerProvider {

  public static PrivacyGroupManager provider() {
    Enclave enclave = Enclave.create();
    PrivacyGroupDAO privacyGroupDAO = PrivacyGroupDAO.create();
    BatchPrivacyGroupPublisher publisher = BatchPrivacyGroupPublisher.create();
    return new PrivacyGroupManagerImpl(enclave, privacyGroupDAO, publisher);
  }
}
