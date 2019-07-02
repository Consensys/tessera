package com.jpmorgan.quorum.mock.servicelocator;

import java.util.Collections;
import java.util.Set;

public class MockServiceLocator implements com.quorum.tessera.service.locator.ServiceLocator {

  private Set<Object> services = Collections.EMPTY_SET;

  public void setServices(Set<Object> services) {
    this.services = services;
  }

  @Override
  public Set<Object> getServices() {
    return services;
  }
}
