package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.data.EncryptedMessage;
import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.messaging.Inbox;
import com.quorum.tessera.messaging.MessageId;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InboxImpl implements Inbox {

  private static final Logger LOGGER = LoggerFactory.getLogger(InboxImpl.class);

  static final String SHA_256 = "SHA-256";

  private final EncryptedMessageDAO dao;
  private MessageDigest sha256;

  InboxImpl(EncryptedMessageDAO dao) {

    this.dao = dao;
    try {
      sha256 = MessageDigest.getInstance(SHA_256);
    } catch (Exception ex) {
      LOGGER.warn("Failed to instantiate a required message digest", ex);
      throw new IllegalStateException(ex);
    }
  }

  MessageHash hash(byte[] encoded) {
    sha256.reset();
    int newLength = 20;
    byte[] array = sha256.digest(encoded);
    byte[] truncatedArray = Arrays.copyOf(array, newLength);
    return new MessageHash(truncatedArray);
  }

  @Override
  public MessageId put(byte[] encoded) {

    final MessageHash messageHash = hash(encoded);
    dao.save(new EncryptedMessage(messageHash, encoded));
    final MessageId messageId = new MessageId(messageHash.getHashBytes());
    LOGGER.info("Received message with identifier: {}", messageId);
    return messageId;
  }

  @Override
  public byte[] get(MessageId messageId) {

    final MessageHash messageHash = new MessageHash(messageId.getValue());
    return dao.retrieveByHash(messageHash).map(EncryptedMessage::getContent).orElse(null);
  }

  @Override
  public Stream<MessageId> stream() {
    return dao.retrieveMessageHashes(0, Integer.MAX_VALUE).stream()
        .map(MessageHash::getHashBytes)
        .map(MessageId::new);
  }

  @Override
  public void delete(MessageId messageId) {

    final MessageHash messageHash = new MessageHash(messageId.getValue());
    dao.delete(messageHash);
  }
}
