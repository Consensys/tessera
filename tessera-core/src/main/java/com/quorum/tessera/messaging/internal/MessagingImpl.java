package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.base64.Base64Codec;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMetadata;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.messaging.Courier;
import com.quorum.tessera.messaging.Inbox;
import com.quorum.tessera.messaging.Message;
import com.quorum.tessera.messaging.MessageId;
import com.quorum.tessera.messaging.Messaging;
import com.quorum.tessera.messaging.NoSuchMessageException;
import com.quorum.tessera.messaging.UnknownRecipientException;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MessagingImpl implements Messaging {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessagingImpl.class);

  static final String SHA_256 = "SHA-256";
  static final String SHA_1 = "SHA-1";

  private final Enclave enclave;
  private final Courier courier;
  private final Inbox inbox;

  private final Base64Codec base64Codec = Base64Codec.create();
  private final PayloadEncoder payloadEncoder = PayloadEncoder.create();

  private MessageDigest sha256, sha1;

  MessagingImpl(Enclave enclave, Courier courier, Inbox inbox) {

    this.enclave = enclave;
    this.courier = courier;
    this.inbox = inbox;
  }

  Inbox getInbox() {
    return this.inbox;
  }

  @Override
  public String send(Message message) {

    LOGGER.debug("Going to try and send {}", message);

    // Look for an early out
    final PublicKey recipient = message.getRecipient();
    if (!courier.isKnownRecipient(recipient)) {
      throw new UnknownRecipientException(recipient);
    }

    // Encrypt the message
    EncodedPayload encrypted =
        enclave.encryptPayload(
            message.getData(),
            message.getSender(),
            Collections.singletonList(recipient),
            PrivacyMetadata.Builder.forStandardPrivate().build());
    final byte[] encoded = payloadEncoder.encode(encrypted);

    // Send it, and return the identifier
    return courier.push(encoded, recipient).toString();
  }

  @Override
  public List<String> received() {
    return getInbox().stream().map(MessageId::toString).collect(Collectors.toList());
  }

  @Override
  public Message read(String messageId) throws NoSuchMessageException {

    // Try and fetch the corresponding message
    final MessageId parsed = MessageId.parseMessageId(messageId);
    byte[] encoded = getInbox().get(parsed);
    if (encoded == null) {
      throw new NoSuchMessageException(parsed);
    }

    // Decode and decrypt it
    final PublicKey publicKey = enclave.defaultPublicKey();
    EncodedPayload payload = payloadEncoder.decode(encoded);
    byte[] decrypted = enclave.unencryptTransaction(payload, publicKey);

    // Wrap it up
    return new Message(payload.getSenderKey(), publicKey, decrypted);
  }

  @Override
  public void remove(String messageId) {
    getInbox().delete(MessageId.parseMessageId(messageId));
  }
}
