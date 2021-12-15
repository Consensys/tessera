package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.PrivateKeyType;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

public class PrivateKeyTypeAdapter extends XmlAdapter<String, PrivateKeyType> {

  private static final Map<String, PrivateKeyType> MAPPING =
      new HashMap<>() {
        {
          put("unlocked", PrivateKeyType.UNLOCKED);
          put("argon2sbox", PrivateKeyType.LOCKED);
        }
      };

  @Override
  public PrivateKeyType unmarshal(final String v) {
    return MAPPING.get(v);
  }

  @Override
  public String marshal(final PrivateKeyType v) {
    return MAPPING.entrySet().stream()
        .filter(e -> e.getValue() == v)
        .map(Map.Entry::getKey)
        .findAny()
        .orElse(null);
  }
}
