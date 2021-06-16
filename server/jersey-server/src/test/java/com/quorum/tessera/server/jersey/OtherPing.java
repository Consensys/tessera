package com.quorum.tessera.server.jersey;

public class OtherPing implements Ping {
  @Override
  public String ping() {
    return "OtherPing";
  }
}
