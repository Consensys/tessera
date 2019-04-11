package com.quorum.tessera.enclave.websockets;

import java.net.URI;

/**
 * FIXME: Need to fix config to handle paths
 */
public interface UriPathAppender {
    
    static URI createFromServerUri(URI serverUri) {
       return URI.create(serverUri.toString() +"/enclave");
    }
    
}
