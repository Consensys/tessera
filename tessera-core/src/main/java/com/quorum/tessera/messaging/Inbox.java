package com.quorum.tessera.messaging;

import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface Inbox {

  /**
   * Parses and stores ("receives") the given encoded message
   *
   * @param encoded a message
   */
  MessageId put(byte[] encoded);

  /**
   * @param messageId a message identifier
   * @return a previously-stored message corresponding to the given identifier
   */
  byte[] get(MessageId messageId);

  /** @return a stream of previously-stored message identifiers */
  Stream<MessageId> stream();

  /**
   * Removes a previously-stored message
   *
   * @param messageId the identifier of the message to remove
   */
  void delete(MessageId messageId);

  static Inbox create() {
    return ServiceLoader.load(Inbox.class).findFirst().get();
  }
}
