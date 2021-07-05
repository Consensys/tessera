package com.quorum.tessera.messaging;

import com.quorum.tessera.data.MessageHash;
import java.util.stream.Stream;

public interface Inbox {

  /**
   * Computes and returns the hash of a message
   *
   * @param encoded a message, encoded
   * @return the hash of the encoded message
   */
  MessageHash hash(byte[] encoded);

  /**
   * Parses and stores ("receives") the given encoded message
   *
   * @param encoded a message
   */
  MessageHash put(byte[] encoded);

  /**
   * @param messageHash a message identifier
   * @return a previously-stored message corresponding to the given identifier
   */
  byte[] get(MessageHash messageHash);

  /** @return a stream of previously-stored message identifiers */
  Stream<MessageHash> stream();

  /**
   * Removes a previously-stored message
   *
   * @param messageHash the hash of the message to remove
   */
  void delete(MessageHash messageHash);
}
