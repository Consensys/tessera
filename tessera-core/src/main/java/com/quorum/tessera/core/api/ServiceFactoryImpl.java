package com.quorum.tessera.core.api;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.transaction.TransactionManager;

public class ServiceFactoryImpl implements ServiceFactory {

  private final ServiceLocator serviceLocator = ServiceLocator.create();

  public ServiceFactoryImpl() {}

  public <T> T find(Class<T> type) {
    return serviceLocator.getServices().stream()
        .filter(type::isInstance)
        .map(type::cast)
        .findAny()
        .orElseThrow(() -> new IllegalStateException("Unable to find service type :" + type));
  }

  @Override
  public TransactionManager transactionManager() {
    return find(TransactionManager.class);
  }

  @Override
  public Config config() {
    return find(Config.class);
  }
}
