package com.quorum.tessera.server.jersey;

import jakarta.inject.Singleton;

@Singleton
public class PongImpl implements Pong {

  public PongImpl() {
    System.out.println("Pong()");
  }

  @Override
  public String pong() {
    return "HEllow";
  }
}
