package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.messaging.*;
import org.junit.Before;
import org.junit.Test;
import com.quorum.tessera.messaging.MessageId;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessagingImplTest {

  private Enclave enclave;
  private Courier courier;
  private Inbox inbox;
  private Message message;
  private MessageId messageId;
  private EncodedPayload encodedPayload;
  private EncryptedMessageDAO dao;

  @Before
  public void setUp() {
    enclave = mock(Enclave.class);
    courier = mock(Courier.class);
    inbox = mock(Inbox.class);
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

    inbox = new Inbox() {
      @Override
      public MessageId put(byte[] encoded) {
        return null;
      }

      @Override
      public byte[] get(MessageId messageId) {
        byte arr[] = {0, 0, 0, 0, 0, 0, 0, 32, 65, -9, -125, 3, 43, 61, 48, -16, -20, -39, 113, -60, -58, -41, 60, -30, 50, -122, 31, 22, 96, -3, -88, -7, -40, 52, -31, -46, -5, 64, -35, 119, 0, 0, 0, 0, 0, 0, 0, 63, -125, 32, -53, -102, 60, -10, -123, 58, -51, 60, 101, 73, 50, 103, 98, -90, -108, 29, 82, 20, -27, 18, -17, -26, 94, 40, 3, -121, 27, 5, 7, -51, 2, -36, -38, -52, -40, 20, -56, 10, -80, -108, 93, 46, 48, -18, 124, 122, 115, -128, 15, 80, 97, 44, 116, -120, 111, 47, -80, -128, -59, -24, -124, 0, 0, 0, 0, 0, 0, 0, 24, 51, 25, -44, -51, -64, -123, -24, 58, 79, 5, 111, -71, 70, 92, -62, -31, -47, 54, -56, -104, -95, 126, -90, 65, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, 20, -49, 80, -41, 81, 111, -61, 44, 28, 113, -94, 92, 102, -54, 96, 35, -122, -110, 20, -30, -91, -123, -86, -103, -32, 124, 72, 53, -7, -77, -109, 124, 29, -102, 108, 70, 12, -125, -63, 67, -26, 34, 112, -19, 109, 107, -42, 73, 0, 0, 0, 0, 0, 0, 0, 24, 13, 85, -109, 70, 101, 0, 31, 35, -85, -66, 116, -104, 121, -111, 91, -26, 11, 64, -48, -49, -87, 13, 41, -3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 32, 65, -9, -125, 3, 43, 61, 48, -16, -20, -39, 113, -60, -58, -41, 60, -30, 50, -122, 31, 22, 96, -3, -88, -7, -40, 52, -31, -46, -5, 64, -35, 119, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        return arr;
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
    impl.read("testings");
  }

  @Test
  public void testRemove() {
    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);
    impl.remove("string");
  }
}
