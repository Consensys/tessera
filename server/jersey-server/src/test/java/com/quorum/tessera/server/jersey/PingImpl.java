package com.quorum.tessera.server.jersey;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Objects;

@Named("myBean")
@Singleton
public class PingImpl implements Ping {

  private Pong pong;

  public PingImpl() {
    this.pong = null;
  }

  @Inject
  public PingImpl(Pong pong) {
    this.pong = Objects.requireNonNull(pong);
    System.out.println("new PingImpl()" + this);
  }

  @PostConstruct
  public void onConstruct() {
    System.out.println("PingImpl.onConstruct " + this);
  }

  @PreDestroy
  public void onDestroy() {
    System.out.println("PingImpl.onDestroy " + this);
  }

  @Override
  public String ping() {
    return pong.pong();
  }
}
