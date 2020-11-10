package com.quorum.tessera.server.jersey;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

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
