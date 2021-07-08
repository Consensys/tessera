package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.messaging.Messaging;
import java.util.Optional;

enum MessagingHolder {
  INSTANCE;

  private Messaging messaging;

  Optional<Messaging> getMessaging() {
    return Optional.ofNullable(messaging);
  }

  Messaging store(Messaging messaging) {
    this.messaging = messaging;
    return messaging;
  }
}
