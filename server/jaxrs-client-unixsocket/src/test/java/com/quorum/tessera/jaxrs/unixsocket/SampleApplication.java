package com.quorum.tessera.jaxrs.unixsocket;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

@ApplicationPath("/")
public class SampleApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return Set.of(SampleResource.class);
  }
}
