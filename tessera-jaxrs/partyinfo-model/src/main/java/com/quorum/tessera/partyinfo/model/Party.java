package com.quorum.tessera.partyinfo.model;

import com.quorum.tessera.partyinfo.URLNormalizer;
import java.util.Objects;

/** Contains a URL of another known node on the network */
public class Party {

  private final String url;

  public Party(final String url) {
    this.url = URLNormalizer.create().normalize(url);
  }

  public String getUrl() {
    return url;
  }

  @Override
  public final boolean equals(final Object o) {
    return (o instanceof Party) && Objects.equals(url, ((Party) o).url);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(url);
  }

  @Override
  public String toString() {
    return "Party{" + "url=" + url + '}';
  }
}
