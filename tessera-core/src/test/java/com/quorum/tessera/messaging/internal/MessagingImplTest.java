package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.base64.Base64Codec;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.KeyManager;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.messaging.*;
import com.quorum.tessera.service.Service;
import junit.framework.TestCase;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quorum.tessera.messaging.MessageId;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MessagingImplTest {

  private Enclave enclave;
  private Courier courier;
  private Inbox inbox;
  private MessagingImpl messagingImpl;
  private Message message;
  private MessageId messageId;
  private EncodedPayload encodedPayload;
  private EncryptedMessageDAO dao;
  private final PayloadEncoder payloadEncoder = new PayloadEncoderImpl();
  private final Base64Codec base64Codec = Base64Codec.create();

  @Before
  public void setUp() {
    enclave = mock(Enclave.class);
    courier = mock(Courier.class);
    inbox = mock(Inbox.class);
    messagingImpl = new MessagingImpl(enclave,courier,inbox);
    message = mock(Message.class);
    messageId = mock(MessageId.class);
    encodedPayload = mock(EncodedPayload.class);
    dao = mock(EncryptedMessageDAO.class);
  }

  @Test
  public void testConstructorWithArgs(){
    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);
    assertThat(impl).isNotNull();
  }

  @Test(expected = UnknownRecipientException.class)
  public void testSendUnknownRecipientExceptionIsThrown(){
    PublicKey sender = PublicKey.from("sender".getBytes());
    PublicKey receiver  = PublicKey.from("receiver".getBytes());

    Message message = new Message(sender,receiver,"data".getBytes());
    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);
    impl.send(message);
  }

  @Test
  public void testSendSuccess() {
    PublicKey sender = PublicKey.from("sender".getBytes());
    PublicKey receiver  = PublicKey.from("receiver".getBytes());
    Message message = new Message(sender,receiver,"data".getBytes());
    doReturn(new MessageId("data".getBytes())).when(courier).push("data".getBytes(),receiver);

    courier = new Courier() {
      @Override
      public boolean isKnownRecipient(PublicKey publicKey) {
        return true;
      }

      @Override
      public MessageId push(byte[] message, PublicKey to) {
        return new MessageId("data".getBytes());
      }
    };

    enclave = new Enclave() {
      @Override
      public PublicKey defaultPublicKey() {
        return null;
      }

      @Override
      public Set<PublicKey> getForwardingKeys() {
        return null;
      }

      @Override
      public Set<PublicKey> getPublicKeys() {
        return null;
      }

      @Override
      public EncodedPayload encryptPayload(byte[] message, PublicKey senderPublicKey, List<PublicKey> recipientPublicKeys, PrivacyMetadata privacyMetadata) {

        final EncodedPayload.Builder payloadBuilder = EncodedPayload.Builder.create();
        return payloadBuilder
          .withSenderKey(senderPublicKey)
          .withCipherText("test".getBytes())
          .withCipherTextNonce(new Nonce("test".getBytes()))
          .withRecipientBoxes(new ArrayList<>())
          .withRecipientNonce(new Nonce("test".getBytes()))
          .withRecipientKeys(recipientPublicKeys)
          .withPrivacyMode(privacyMetadata.getPrivacyMode())
          .withAffectedContractTransactions(new HashMap<>())
          .withExecHash(privacyMetadata.getExecHash())
          .withMandatoryRecipients(privacyMetadata.getMandatoryRecipients())
          .build();
      }

      @Override
      public EncodedPayload encryptPayload(RawTransaction rawTransaction, List<PublicKey> recipientPublicKeys, PrivacyMetadata privacyMetadata) {
        return null;
      }

      @Override
      public Set<TxHash> findInvalidSecurityHashes(EncodedPayload encodedPayload, List<AffectedTransaction> affectedContractTransactions) {
        return null;
      }

      @Override
      public RawTransaction encryptRawPayload(byte[] message, PublicKey sender) {
        return null;
      }

      @Override
      public byte[] unencryptTransaction(EncodedPayload payload, PublicKey providedKey) {
        return new byte[0];
      }

      @Override
      public byte[] unencryptRawPayload(RawTransaction payload) {
        return new byte[0];
      }

      @Override
      public byte[] createNewRecipientBox(EncodedPayload payload, PublicKey recipientKey) {
        return new byte[0];
      }

      @Override
      public Status status() {
        return null;
      }
    };
    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);
    impl.send(message);

  }

  @Test
  public void testReceived(){
    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);
    assertThat(impl.received()).isNotNull();
  }

  @Test(expected = NoSuchMessageException.class)
  public void testReadThrowsNoSuchMessageException(){
    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);
    impl.read("abcd");
  }

  @Test
  public void testReadSuccess() {
    PublicKey sender = PublicKey.from("sender".getBytes());
    PublicKey receiver  = PublicKey.from("receiver".getBytes());

    Message message = new Message(sender,receiver,"data".getBytes());
    PayloadEncoder payloadEncoder = new PayloadEncoderImpl() {
      @Override
      public byte[] encode(EncodedPayload payload) {
        return new byte[0];
      }

      @Override
      public EncodedPayload decode(byte[] input) {
        final EncodedPayload.Builder payloadBuilder = EncodedPayload.Builder.create();
        final PrivacyMetadata privacyMetadata =
          PrivacyMetadata.Builder.create().withPrivacyMode(PrivacyMode.STANDARD_PRIVATE).build();
        return payloadBuilder
          .withSenderKey(sender)
          .withCipherText("test".getBytes())
          .withCipherTextNonce(new Nonce("test".getBytes()))
          .withRecipientBoxes(new ArrayList<>())
          .withRecipientNonce(new Nonce("test".getBytes()))
          .withRecipientKeys(new ArrayList<>())
          .withPrivacyMode(privacyMetadata.getPrivacyMode())
          .withAffectedContractTransactions(new HashMap<>())
          .withExecHash(privacyMetadata.getExecHash())
          .withMandatoryRecipients(privacyMetadata.getMandatoryRecipients())
          .build();
      }

      @Override
      public EncodedPayloadCodec encodedPayloadCodec() {
        return null;
      }
    };
    enclave = new Enclave() {
      @Override
      public PublicKey defaultPublicKey() {
        return sender;
      }

      @Override
      public Set<PublicKey> getForwardingKeys() {
        return null;
      }

      @Override
      public Set<PublicKey> getPublicKeys() {
        return null;
      }

      @Override
      public EncodedPayload encryptPayload(byte[] message, PublicKey senderPublicKey, List<PublicKey> recipientPublicKeys, PrivacyMetadata privacyMetadata) {

        final EncodedPayload.Builder payloadBuilder = EncodedPayload.Builder.create();
        return payloadBuilder
          .withSenderKey(senderPublicKey)
          .withCipherText("test".getBytes())
          .withCipherTextNonce(new Nonce("test".getBytes()))
          .withRecipientBoxes(new ArrayList<>())
          .withRecipientNonce(new Nonce("test".getBytes()))
          .withRecipientKeys(recipientPublicKeys)
          .withPrivacyMode(privacyMetadata.getPrivacyMode())
          .withAffectedContractTransactions(new HashMap<>())
          .withExecHash(privacyMetadata.getExecHash())
          .withMandatoryRecipients(privacyMetadata.getMandatoryRecipients())
          .build();
      }

      @Override
      public EncodedPayload encryptPayload(RawTransaction rawTransaction, List<PublicKey> recipientPublicKeys, PrivacyMetadata privacyMetadata) {
        return null;
      }

      @Override
      public Set<TxHash> findInvalidSecurityHashes(EncodedPayload encodedPayload, List<AffectedTransaction> affectedContractTransactions) {
        return null;
      }

      @Override
      public RawTransaction encryptRawPayload(byte[] message, PublicKey sender) {
        return null;
      }

      @Override
      public byte[] unencryptTransaction(EncodedPayload payload, PublicKey providedKey) {
        return "test".getBytes();
      }

      @Override
      public byte[] unencryptRawPayload(RawTransaction payload) {
        return new byte[0];
      }

      @Override
      public byte[] createNewRecipientBox(EncodedPayload payload, PublicKey recipientKey) {
        return new byte[0];
      }

      @Override
      public Status status() {
        return null;
      }
    };
    inbox = new Inbox() {
      @Override
      public MessageId put(byte[] encoded) {
        return null;
      }

      @Override
      public byte[] get(MessageId messageId) {
        return "1234567".getBytes();
      }

      @Override
      public Stream<MessageId> stream() {
        return null;
      }

      @Override
      public void delete(MessageId messageId) {

      }
    };

    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);
    impl.read("test");

  }

  @Test
  public void testRemove() {
    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);
    impl.remove("string");
  }

}
