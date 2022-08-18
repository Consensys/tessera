package com.quorum.tessera.key.vault;

import java.util.Map;

public class SetSecretResponse {

  Map<String, String> properties;

  public SetSecretResponse(Map<String, String> properties) {
    this.properties = properties;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public String getProperty(String name) {
    return properties.get(name);
  }
}
