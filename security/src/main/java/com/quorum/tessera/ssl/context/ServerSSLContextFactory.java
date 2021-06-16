package com.quorum.tessera.ssl.context;

import java.util.ServiceLoader;

public interface ServerSSLContextFactory extends SSLContextFactory {

  static SSLContextFactory create() {
    return ServiceLoader.load(ServerSSLContextFactory.class).findFirst().get();
  }
}
