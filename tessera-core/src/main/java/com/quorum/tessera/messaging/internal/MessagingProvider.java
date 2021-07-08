package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.messaging.Courier;
import com.quorum.tessera.messaging.Inbox;
import com.quorum.tessera.messaging.Messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagingProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessagingProvider.class);

  public static Messaging provider() {
    final MessagingHolder holder = MessagingHolder.INSTANCE;
    if (holder.getMessaging().isPresent()) {
      return holder.getMessaging().get();
    }

    final Enclave enclave = Enclave.create();
    final Courier courier = Courier.create();
    final Inbox inbox = Inbox.create();

    return holder.store(new MessagingImpl(enclave, courier, inbox));
  }
}
