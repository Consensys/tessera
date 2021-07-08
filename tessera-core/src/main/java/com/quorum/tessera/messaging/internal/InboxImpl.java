package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.data.EncryptedMessage;
import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.messaging.Inbox;
import java.security.MessageDigest;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InboxImpl implements Inbox {

  private static final Logger LOGGER = LoggerFactory.getLogger(InboxImpl.class);

  static final String SHA_256 = "SHA-256";
  static final String SHA_1 = "SHA-1";

  private final EncryptedMessageDAO dao;
  private MessageDigest sha256, sha1;

  InboxImpl(EncryptedMessageDAO dao) {

    this.dao = dao;
    try {
      sha256 = MessageDigest.getInstance(SHA_256);
      sha1 = MessageDigest.getInstance(SHA_1);
    } catch (Exception ex) {
      LOGGER.warn("Failed to instantiate a required message digest", ex);
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public MessageHash hash(byte[] encoded) {
    sha256.reset();
    sha1.reset();
    return new MessageHash(sha1.digest(sha256.digest(encoded)));
  }

  @Override
  public MessageHash put(byte[] encoded) {

    final MessageHash messageHash = hash(encoded);
    LOGGER.debug("Received message with identifier: {}", messageHash);
    dao.save(new EncryptedMessage(messageHash, encoded));
    return messageHash;
  }

  @Override
  public byte[] get(MessageHash messageHash) {
    return dao.retrieveByHash(messageHash).map(EncryptedMessage::getContent).orElse(null);
  }

  @Override
  public Stream<MessageHash> stream() {
    return dao.retrieveMessageHashes(0, Integer.MAX_VALUE).stream();
  }

  @Override
  public void delete(MessageHash messageHash) {
    dao.delete(messageHash);
  }
}
