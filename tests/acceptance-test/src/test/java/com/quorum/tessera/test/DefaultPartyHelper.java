package com.quorum.tessera.test;

import config.ConfigDescriptor;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suite.ExecutionContext;
import suite.Utils;

public class DefaultPartyHelper implements PartyHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPartyHelper.class);

  private final List<Party> parties = new ArrayList<>();

  public DefaultPartyHelper() {

    ExecutionContext executionContext = ExecutionContext.currentContext();

    if (executionContext.getConfigs().isEmpty()) {
      LOGGER.error("No parties found");
      throw new IllegalStateException("No parties found");
    }

    for (ConfigDescriptor c : executionContext.getConfigs()) {
      String key = c.getKey().getPublicKey();
      URL file = Utils.toUrl(c.getPath());
      String alias = c.getAlias().name();

      parties.add(new Party(key, file, alias));
      LOGGER.trace("Key: {}, File: {}, Alias: {}", key, file, alias);
    }
  }

  @Override
  public Stream<Party> getParties() {
    return parties.stream();
  }
}
