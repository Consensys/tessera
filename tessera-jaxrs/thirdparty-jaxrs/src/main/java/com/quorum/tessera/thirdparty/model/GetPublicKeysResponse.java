package com.quorum.tessera.thirdparty.model;

import java.util.List;

// TODO(cjh) just used for swagger generation - should be used in the actual jaxrs methods
public class GetPublicKeysResponse {

  private List<Key> keys;

  public List<Key> getKeys() {
    return keys;
  }

  public void setKeys(List<Key> keys) {
    this.keys = keys;
  }
}
