package com.quorum.tessera.privacygroup;

public class ResidentGroupHandlerProvider {

  public static ResidentGroupHandler provider() {
    final PrivacyGroupManager privacyGroupManager = PrivacyGroupManager.create();
    return new ResidentGroupHandlerImpl(privacyGroupManager);
  }
}
