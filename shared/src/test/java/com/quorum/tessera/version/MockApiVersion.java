package com.quorum.tessera.version;

public class MockApiVersion implements ApiVersion {
  @Override
  public String getVersion() {
    return "0.1";
  }
}
