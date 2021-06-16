package com.quorum.tessera.privacygroup.internal;

import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.privacygroup.ResidentGroupHandler;

public class ResidentGroupHandlerProvider {

  public static ResidentGroupHandler provider() {
    final PrivacyGroupManager privacyGroupManager = PrivacyGroupManager.create();
    return new ResidentGroupHandlerImpl(privacyGroupManager);
  }
}
