package com.quorum.tessera.version;

public class MultiTenancyVersion implements ApiVersion {

  public static final String API_VERSION_2_1 = "2.1";

  public static final String MIME_TYPE_JSON_2_1 = "application/vnd.tessera-2.1+json";

  @Override
  public String getVersion() {
    return API_VERSION_2_1;
  }
}
