package com.quorum.tessera.enclave;

import java.util.stream.Stream;

public enum PrivacyMode {
  STANDARD_PRIVATE(0),
  PARTY_PROTECTION(1),
  MANDATORY_RECIPIENTS(2),
  PRIVATE_STATE_VALIDATION(3);

  private final int privacyFlag;

  PrivacyMode(int privacyFlag) {
    this.privacyFlag = privacyFlag;
  }

  public static PrivacyMode fromFlag(int privacyFlag) {
    return Stream.of(PrivacyMode.values())
        .filter(v -> v.getPrivacyFlag() == privacyFlag)
        .findFirst()
        .orElse(PrivacyMode.STANDARD_PRIVATE);
  }

  public int getPrivacyFlag() {
    return this.privacyFlag;
  }
}
