package com.quorum.tessera.partyinfo;

import java.net.URI;
import java.net.URISyntaxException;

public interface URLNormalizer {

  default String normalize(String url) {
    try {
      final URI uri = new URI(url).normalize();
      if (uri.getPath().isEmpty()) {
        return url + "/";
      }
    } catch (URISyntaxException ex) {
      throw new RuntimeException(String.format("Invalid URL %s", url));
    }
    return url;
  }

  static URLNormalizer create() {
    return new URLNormalizer() {};
  }
}
