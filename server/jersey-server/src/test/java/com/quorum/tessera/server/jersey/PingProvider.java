package com.quorum.tessera.server.jersey;

public class PingProvider {

  public static Ping provider() {

    return new Ping() {
      @Override
      public String ping() {
        return "PING";
      }
    };
  }
}
