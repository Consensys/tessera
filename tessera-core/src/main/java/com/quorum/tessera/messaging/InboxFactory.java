package com.quorum.tessera.messaging;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import java.util.Optional;

public interface InboxFactory {

  Inbox create(Config config);

  Optional<Inbox> inbox();

  static InboxFactory create() {
    return ServiceLoaderUtil.load(InboxFactory.class).orElse(DefaultInboxFactory.INSTANCE);
  }
}
