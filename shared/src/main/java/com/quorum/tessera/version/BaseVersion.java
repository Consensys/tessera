package com.quorum.tessera.version;

public class BaseVersion implements ApiVersion {

  public static final String API_VERSION_1 = "v1";

  @Override
  public String getVersion() {
    return API_VERSION_1;
  }
}
