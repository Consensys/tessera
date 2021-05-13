package com.quorum.tessera.service;

public interface Service {

  enum Status {
    STARTED,
    STOPPED
  }

  void start();

  void stop();

  Status status();
}
