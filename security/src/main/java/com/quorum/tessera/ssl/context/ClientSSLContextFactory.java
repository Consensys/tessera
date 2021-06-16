package com.quorum.tessera.ssl.context;

import java.util.ServiceLoader;

public interface ClientSSLContextFactory extends SSLContextFactory {

  static SSLContextFactory create() {
    return ServiceLoader.load(ClientSSLContextFactory.class).findFirst().get();
  }
}
