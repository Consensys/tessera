package com.quorum.tessera.version;

public class CBORSupportVersion implements ApiVersion {

  public static final String API_VERSION_5 = "5.0";

  @Override
  public String getVersion() {
    return API_VERSION_5;
  }
}
