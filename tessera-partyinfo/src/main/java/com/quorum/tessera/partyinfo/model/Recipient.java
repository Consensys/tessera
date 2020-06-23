package com.quorum.tessera.partyinfo.model;

import com.quorum.tessera.encryption.PublicKey;

import java.util.Objects;

/** Contains a mapping of a public key to URL that is on the same network */
public class Recipient {

    private final PublicKey key;

    private final String url;

    private final boolean acceptsEnhancedPrivacy;

    private Recipient(final PublicKey key, final String url, final boolean acceptsEnhancedPrivacy) {
        this.key = key;
        this.url = url;
        this.acceptsEnhancedPrivacy = acceptsEnhancedPrivacy;
    }

    public static Recipient of(final PublicKey key, final String url) {
        return new Recipient(key, url, false);
    }

    public static Recipient of(final PublicKey key, final String url, final boolean acceptsEnhancedPrivacy) {
        return new Recipient(key, url, acceptsEnhancedPrivacy);
    }

    public static Recipient from(final Recipient recipient, final boolean acceptsEnhancedPrivacy) {
        return new Recipient(recipient.getKey(), recipient.getUrl(), acceptsEnhancedPrivacy);
    }

    public PublicKey getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public boolean acceptsEnhancedPrivacy() {
        return acceptsEnhancedPrivacy;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Recipient)) {
            return false;
        }

        final Recipient recipient = (Recipient) o;

        return Objects.equals(key, recipient.key) && Objects.equals(url, recipient.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, url);
    }

    @Override
    public String toString() {
        return "Recipient{" + "key=" + key + ", url='" + url + '\'' + '}';
    }
}
