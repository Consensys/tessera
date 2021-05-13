package com.quorum.tessera.ssl.context;

import com.quorum.tessera.ServiceLoaderUtil;

public interface ClientSSLContextFactory extends SSLContextFactory {

  static SSLContextFactory create() {
    // TODO: return the stream and let the caller deal with it
    return ServiceLoaderUtil.loadAll(ClientSSLContextFactory.class).findAny().get();
  }
}
