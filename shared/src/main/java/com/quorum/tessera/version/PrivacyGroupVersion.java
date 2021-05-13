package com.quorum.tessera.version;

public class PrivacyGroupVersion implements ApiVersion {

  public static final String API_VERSION_3 = "3.0";

  public static final String MIME_TYPE_JSON_3 = "application/vnd.tessera-3.0+json";

  @Override
  public String getVersion() {
    return API_VERSION_3;
  }
}
