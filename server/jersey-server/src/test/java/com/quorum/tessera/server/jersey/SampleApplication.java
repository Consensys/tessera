package com.quorum.tessera.server.jersey;

import java.util.Set;
import javax.ws.rs.core.Application;

public class SampleApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(SampleResource.class);
    }
}
