package com.quorum.tessera.messaging;

import java.util.List;

public interface Messaging {

  /**
   * Sends the given message to the recipient indicated by their public key
   *
   * @param message the message to be sent
   * @return the identifier of the message that was sent
   */
  String send(Message message);

  /** @return a list of identifier of received messages */
  List<String> received();

  /**
   * @param messageId the identifier of a message in the inbox
   * @return the corresponding message, decrypted
   * @throws NoSuchMessageException if the message identifier does not refer to a stored message
   */
  Message read(String messageId) throws NoSuchMessageException;

  /**
   * Removes the identified message from persistent storage
   *
   * @param messageId the identifier of a store message
   */
  void remove(String messageId);
}
