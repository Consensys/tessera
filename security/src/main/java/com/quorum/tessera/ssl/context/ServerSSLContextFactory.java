package com.quorum.tessera.ssl.context;

import com.quorum.tessera.ServiceLoaderUtil;

public interface ServerSSLContextFactory extends SSLContextFactory {

    static SSLContextFactory create() {
        // TODO: return the stream and let the caller deal with it
        return ServiceLoaderUtil.loadAll(ServerSSLContextFactory.class).findAny().get();
    }
}
