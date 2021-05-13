package com.quorum.tessera.test;

import java.util.stream.Stream;
import suite.NodeAlias;

public interface PartyHelper {

  Stream<Party> getParties();

  default Party findByAlias(NodeAlias alias) {
    return getParties()
        .filter(p -> p.getAlias().equals(alias.name()))
        .findAny()
        .orElseThrow(() -> new RuntimeException("No party found with alias " + alias));
  }

  default Party findByAlias(String alias) {
    return findByAlias(NodeAlias.valueOf(alias));
  }

  default Party findByPublicKey(String publicKey) {
    return getParties()
        .filter(p -> p.getPublicKey().equals(publicKey))
        .findAny()
        .orElseThrow(() -> new RuntimeException("No party found with publicKey " + publicKey));
  }

  static PartyHelper create() {
    return new DefaultPartyHelper();
  }
}
