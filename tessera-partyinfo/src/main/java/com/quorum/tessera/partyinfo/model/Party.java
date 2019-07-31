package com.quorum.tessera.partyinfo.model;


import com.quorum.tessera.partyinfo.URLNormalizer;
import java.time.Instant;
import java.util.Objects;

/**
 * Contains a URL of another known node on the network
 */
public class Party {

    private final String url;

    private Instant lastContacted;

    public Party(final String url) {
        this.url = URLNormalizer.create().normalize(url);
    }

    public String getUrl() {
        return url;
    }

    public Instant getLastContacted() {
        return lastContacted;
    }

    public void setLastContacted(final Instant lastContacted) {
        this.lastContacted = lastContacted;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.url);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Party other = (Party) obj;
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Party{" + "url=" + url + ", lastContacted=" + lastContacted + '}';
    }




}
