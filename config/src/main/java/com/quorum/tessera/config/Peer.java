package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.ValidUrl;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public class Peer extends ConfigItem {

  @ValidUrl
  @NotNull
  @XmlElement(required = true)
  private String url;

  public Peer(String url) {
    this.url = url;
  }

  public Peer() {}

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Peer peer = (Peer) o;
    return Objects.equals(url, peer.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), url);
  }
}
