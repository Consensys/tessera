package com.quorum.tessera.version;

public class MandatoryRecipientsVersion implements ApiVersion {

  public static final String API_VERSION_4 = "4.0";

  public static final String MIME_TYPE_JSON_4 = "application/vnd.tessera-4.0+json";

  @Override
  public String getVersion() {
    return API_VERSION_4;
  }
}
