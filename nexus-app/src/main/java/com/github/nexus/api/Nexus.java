package com.github.nexus.api;

import com.github.nexus.service.locator.ServiceLocator;

import javax.ws.rs.core.Application;
import java.util.Objects;
import java.util.Set;
import javax.ws.rs.ApplicationPath;

@Logged
@ApplicationPath("/")
public class Nexus extends Application {
    
    private final ServiceLocator serviceLocator;

    private final String contextName;

    public Nexus(final ServiceLocator serviceLocator, final String contextName) {
        this.serviceLocator = Objects.requireNonNull(serviceLocator);
        this.contextName = Objects.requireNonNull(contextName);
    }
    
    @Override
    public Set<Object> getSingletons() {
       return serviceLocator.getServices(contextName);
    }
    
}
