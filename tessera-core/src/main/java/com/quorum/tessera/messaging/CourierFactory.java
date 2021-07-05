package com.quorum.tessera.messaging;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

public interface CourierFactory {

  Courier create(Config config);

  CommunicationType getCommunicationType();

  static CourierFactory newFactory(Config config) {

    final CommunicationType commType = config.getP2PServerConfig().getCommunicationType();
    return ServiceLoaderUtil.loadAll(CourierFactory.class)
        .filter(f -> f.getCommunicationType() == commType)
        .findAny()
        .orElseThrow(
            () ->
                new UnsupportedOperationException(
                    "Unable to create a PayloadPublisherFactory for " + commType));
  }
}
