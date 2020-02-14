package com.quorum.tessera.context;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.ServerConfig;

import javax.ws.rs.client.Client;

public interface RestClientFactory {

    Client buildFrom(ServerConfig serverContext);

    static RestClientFactory create() {
        return ServiceLoaderUtil.load(RestClientFactory.class).get();
    }
}
