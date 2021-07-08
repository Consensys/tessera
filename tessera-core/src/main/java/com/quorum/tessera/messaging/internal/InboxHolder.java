package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.messaging.Inbox;
import java.util.Optional;

enum InboxHolder {
  INSTANCE;

  private Inbox inbox;

  Optional<Inbox> getInbox() {
    return Optional.ofNullable(inbox);
  }

  Inbox store(Inbox inbox) {
    this.inbox = inbox;
    return inbox;
  }
}
