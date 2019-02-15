package com.quorum.tessera.service;

public interface Service {

    enum Status {
        STARTING,
        STARTED,
        STOPPING,
        STOPPED

    }

    void start();

    void stop();

    Status status();

}
