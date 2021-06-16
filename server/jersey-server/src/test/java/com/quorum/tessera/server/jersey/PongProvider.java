package com.quorum.tessera.server.jersey;

public class PongProvider {

  public static Pong provider() {
    return new Pong() {
      @Override
      public String pong() {
        return "PONG";
      }
    };
  }
}
