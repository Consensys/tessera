package com.quorum.tessera.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import java.util.ServiceLoader;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface TesseraServerFactory<T> {

  Logger LOGGER = LoggerFactory.getLogger(TesseraServerFactory.class);

  TesseraServer createServer(ServerConfig config, Set<T> services);

  CommunicationType communicationType();

  static TesseraServerFactory create(CommunicationType communicationType) {
    LOGGER.debug("Creating TesseraServerFactory for {}", communicationType);

    return ServiceLoader.load(TesseraServerFactory.class).stream()
        .map(ServiceLoader.Provider::get)
        .filter(f -> f.communicationType() == communicationType)
        .peek(
            tesseraServerFactory ->
                LOGGER.debug("Found factory {} for {}", tesseraServerFactory, communicationType))
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException("No server factory found for " + communicationType));
  }
}
