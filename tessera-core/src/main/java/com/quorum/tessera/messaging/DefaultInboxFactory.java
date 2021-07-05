package com.quorum.tessera.messaging;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

enum DefaultInboxFactory implements InboxFactory {
  INSTANCE;

  private static final AtomicReference<Inbox> REF = new AtomicReference<>();

  @Override
  public Inbox create(Config config) {

    if (Objects.nonNull(REF.get())) {
      return REF.get();
    }

    EntityManagerDAOFactory entityManagerDAOFactory = EntityManagerDAOFactory.newFactory(config);
    EncryptedMessageDAO encryptedMessageDAO = entityManagerDAOFactory.createEncryptedMessageDAO();

    final Inbox inbox = new InboxImpl(encryptedMessageDAO);

    REF.set(inbox);
    return inbox;
  }

  @Override
  public Optional<Inbox> inbox() {
    return Optional.ofNullable(REF.get());
  }
}
