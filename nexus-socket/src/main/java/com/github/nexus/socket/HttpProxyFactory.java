package com.github.nexus.socket;

import java.net.URI;

public class HttpProxyFactory {

    
    public HttpProxy create(URI serverUri) {
        return new HttpProxy(serverUri,new SocketFactory());
    }
}
