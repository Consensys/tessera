package com.quorum.tessera.version;

public class EnhancedPrivacyVersion implements ApiVersion {

  public static final String API_VERSION_2 = "v2";

  @Override
  public String getVersion() {
    return API_VERSION_2;
  }
}
