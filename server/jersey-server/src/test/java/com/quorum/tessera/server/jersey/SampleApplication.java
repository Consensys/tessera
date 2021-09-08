package com.quorum.tessera.server.jersey;

import jakarta.ws.rs.core.Application;
import java.util.Set;

public class SampleApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return Set.of(SampleResource.class);
  }
}
