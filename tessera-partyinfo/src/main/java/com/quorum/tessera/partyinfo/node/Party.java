package com.quorum.tessera.partyinfo.node;


import com.quorum.tessera.partyinfo.URLNormalizer;

import java.util.Objects;

/**
 * Contains a URL of another known node on the network
 */
public class Party {

    private final String url;

    public Party(final String url) {
        this.url = URLNormalizer.create().normalize(url);
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Party party = (Party) o;
        return url.equals(party.url);
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
