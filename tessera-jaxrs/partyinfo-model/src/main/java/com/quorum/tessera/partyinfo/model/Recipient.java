package com.quorum.tessera.partyinfo.model;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Objects;

/** Contains a mapping of a public key to URL that is on the same network */
public class Recipient {

  private final PublicKey key;

  private final String url;

  private Recipient(final PublicKey key, final String url) {
    this.key = key;
    this.url = url;
  }

  public static Recipient of(final PublicKey key, final String url) {
    return new Recipient(key, url);
  }

  public static Recipient from(final Recipient recipient) {
    return new Recipient(recipient.getKey(), recipient.getUrl());
  }

  public PublicKey getKey() {
    return key;
  }

  public String getUrl() {
    return url;
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
