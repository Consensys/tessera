package com.quorum.tessera.core.api;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.transaction.TransactionManager;

public interface ServiceFactory {

  TransactionManager transactionManager();

  Config config();

  static ServiceFactory create() {
    return ServiceLoaderUtil.load(ServiceFactory.class).orElse(new ServiceFactoryImpl());
  }
}
