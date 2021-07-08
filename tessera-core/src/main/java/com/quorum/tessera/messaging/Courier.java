package com.quorum.tessera.messaging;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;

public interface Courier {

  /**
   * @param publicKey the public key of a putative message recipient
   * @return <code>true</code> if the recipient is known; <code>false</code> otherwise
   */
  boolean isKnownRecipient(PublicKey publicKey);

  /**
   * Pushes the given message to the recipient corresponding to the given public key
   *
   * @param message a message
   * @param to the public key of the recipient
   * @return the binary form of the sent message
   */
  MessageId push(byte[] message, PublicKey to);

  static Courier create() {
    return ServiceLoaderUtil.loadSingle(ServiceLoader.load(Courier.class));
  }
}
