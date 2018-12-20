package com.quorum.tessera.node.model;

import java.net.URI;
import java.util.Objects;

/**
 * Contains a URL of another known node on the network
 */
public class Party {

    private final URI url;

    public Party(final URI url) {
        this.url = url;
    }

    public Party(final String url) {
        this(URI.create(url));
    }

    public URI getUrl() {
        return url;
    }

    @Override
    public boolean equals(final Object o) {
        return (o instanceof Party) && Objects.equals(url, ((Party) o).url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return "Party{" + "url=" + url + '}';
    }
    
    

}
