package com.quorum.tessera.messaging;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

enum DefaultMessagingFactory implements MessagingFactory {
  INSTANCE;

  private static final AtomicReference<Messaging> REF = new AtomicReference<>();

  @Override
  public Messaging create(Config config) {

    if (Objects.nonNull(REF.get())) {
      return REF.get();
    }

    Enclave enclave = EnclaveFactory.create().create(config);
    Courier courier = CourierFactory.newFactory(config).create(config);
    Inbox inbox = InboxFactory.create().create(config);

    final Messaging messaging = new MessagingImpl(enclave, courier, inbox);

    REF.set(messaging);
    return messaging;
  }

  @Override
  public Optional<Messaging> messaging() {
    return Optional.ofNullable(REF.get());
  }
}
