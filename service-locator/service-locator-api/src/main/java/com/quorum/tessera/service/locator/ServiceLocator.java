package com.quorum.tessera.service.locator;

import java.util.ServiceLoader;
import java.util.Set;

/** Creates a set of services that are configured */
public interface ServiceLocator {

  /**
   * Retrieves all the services specified in the configuration file
   *
   * @return the set of all configuration services
   */
  Set<Object> getServices();

  /**
   * Returns an implementation of the {@link ServiceLocator} from the service loader
   *
   * @return the {@link ServiceLocator} instance
   */
  static ServiceLocator create() {
    return ServiceLoader.load(ServiceLocator.class).iterator().next();
  }
}
