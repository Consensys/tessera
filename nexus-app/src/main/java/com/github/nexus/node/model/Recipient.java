package com.github.nexus.node.model;

import com.github.nexus.nacl.Key;

import java.util.Objects;

public class Recipient {

    private final Key key;
    
    private final String url;

    public Recipient(final Key key, final String url) {
        this.key = key;
        this.url = url;
    }

    public Key getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Recipient)) {
            return false;
        }

        final Recipient recipient = (Recipient) o;

        return Objects.equals(key, recipient.key) &&
            Objects.equals(url, recipient.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, url);
    }
}
