
package com.quorum.tessera.test;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;


public enum Party {
    
    ONE("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=",8080),
    TWO("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=",8081),
    THREE("giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=",8082),
    FOUR("Tj8xg/HpsYmh7Te3UerzlLx1HgpWVOGq25ZgbwaPNVM=",8083);
    
    private final String publicKey;

    private final URI uri;
    
    Party(String publicKey,int port) {
        this.publicKey = publicKey;
        this.uri = UriBuilder.fromUri("http://127.0.0.1").port(port).build();
    }

    public String getPublicKey() {
        return publicKey;
    }

    public URI getUri() {
        return uri;
    }

}
