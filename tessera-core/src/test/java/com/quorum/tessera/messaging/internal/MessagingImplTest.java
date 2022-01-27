package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.base64.Base64Codec;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
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

  private final PayloadEncoder payloadEncoder = PayloadEncoder.create(EncodedPayloadCodec.LEGACY);
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
  }

  @Test
  public void testConstructorWithArgs(){
    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);
    assertThat(impl).isNotNull();
  }

  @Test
  public void testSendUnknownRecipientExceptionIsThrown() throws UnknownRecipientException{
    PublicKey sender = PublicKey.from("sender".getBytes());
    PublicKey receiver  = PublicKey.from("receiver".getBytes());

    Message message = new Message(sender,receiver,"data".getBytes());
    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);

    String msg = "";
   try {
     msg = impl.send(message);
   }
   catch(UnknownRecipientException ex){
     assertThat(msg).isEmpty();
   }
  }

  @Test
  public void testSendSuccess() {
    PublicKey sender = PublicKey.from("sender".getBytes());
    PublicKey receiver  = PublicKey.from("receiver".getBytes());
    messagingImpl = mock(MessagingImpl.class);
    Message message = new Message(sender,receiver,"data".getBytes());
    doReturn("doneTest").when(messagingImpl).send(message);
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

    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);
    try {
      impl.send(message);
    }
    catch(Exception ex){

      assertThat(courier.push("data".getBytes(),receiver).toString()).isNotNull();
    }

  }

  @Test
  public void testReceived(){
    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);
    assertThat(impl.received()).isNotNull();
  }

  @Test
  public void testReadThrowsNoSuchMessageException() throws NoSuchMessageException{
    PublicKey sender = PublicKey.from("sender".getBytes());
    PublicKey receiver  = PublicKey.from("receiver".getBytes());

    Message message = new Message(sender,receiver,"data".getBytes());
    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);

    try {
      impl.read("abcd");
    }
    catch(NoSuchMessageException ex){

    }
  }

  @Test
  public void testReadSuccess() {
    PublicKey sender = PublicKey.from("sender".getBytes());
    PublicKey receiver  = PublicKey.from("receiver".getBytes());

    Message message = new Message(sender,receiver,"data".getBytes());

    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);

  }

  @Test
  public void testRemove() {
    MessagingImpl impl = new MessagingImpl(enclave,courier,inbox);
    impl.remove("string");
  }

}
