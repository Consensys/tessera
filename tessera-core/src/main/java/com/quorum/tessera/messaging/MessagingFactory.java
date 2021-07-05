package com.quorum.tessera.messaging;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import java.util.Optional;

public interface MessagingFactory {

  Messaging create(Config config);

  Optional<Messaging> messaging();

  static MessagingFactory create() {
    return ServiceLoaderUtil.load(MessagingFactory.class).orElse(DefaultMessagingFactory.INSTANCE);
  }
}
