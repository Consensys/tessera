package com.quorum.tessera.p2p.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;

// TODO(cjh) just used for swagger generation - should be used in the actual jaxrs methods
public class GetPartyInfoResponse {

  @Schema(description = "server's url")
  @XmlElement
  private String url;

  @Schema() @XmlElement private List<Key> keys;

  @XmlElement private List<Peer> peers;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public List<Key> getKeys() {
    return keys;
  }

  public void setKeys(List<Key> keys) {
    this.keys = keys;
  }

  public List<Peer> getPeers() {
    return peers;
  }

  public void setPeers(List<Peer> peers) {
    this.peers = peers;
  }
}
