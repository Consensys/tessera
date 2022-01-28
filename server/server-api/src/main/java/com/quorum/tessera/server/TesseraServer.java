package com.quorum.tessera.server;

import com.quorum.tessera.config.AppType;
import java.net.URI;

public interface TesseraServer {

  void start() throws Exception;

  void stop() throws Exception;

  URI getUri();

  AppType getAppType();
}
