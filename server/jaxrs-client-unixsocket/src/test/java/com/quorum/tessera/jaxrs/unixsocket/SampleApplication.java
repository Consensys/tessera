package com.quorum.tessera.jaxrs.unixsocket;

import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class SampleApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(SampleResource.class);
    }
}
