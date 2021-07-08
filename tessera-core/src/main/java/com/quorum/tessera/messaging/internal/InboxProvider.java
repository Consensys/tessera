package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.messaging.Inbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboxProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(InboxProvider.class);

  public static Inbox provider() {
    final InboxHolder inboxHolder = InboxHolder.INSTANCE;
    if (inboxHolder.getInbox().isPresent()) {
      return inboxHolder.getInbox().get();
    }

    final EncryptedMessageDAO encryptedMessageDAO = EncryptedMessageDAO.create();
    return inboxHolder.store(new InboxImpl(encryptedMessageDAO));
  }
}
