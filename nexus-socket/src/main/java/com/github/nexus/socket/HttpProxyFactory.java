package com.github.nexus.socket;

import java.net.URI;
import java.util.Objects;

public class HttpProxyFactory {

    private final URI serverUri;

    public HttpProxyFactory(final URI serverUri) {
        this.serverUri = Objects.requireNonNull(serverUri);
    }

    public HttpProxy create() {
        return new HttpProxy(serverUri, new SocketFactory());
    }

}
