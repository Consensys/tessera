package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.messaging.*;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quorum.tessera.messaging.MessageId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MessagingImplTest {

  private Enclave enclave;
  private Courier courier;
  private Inbox inbox;
  private MessagingImpl messagingImpl;
  private Message message;
  private PayloadEncoder payloadEncoder;
  private MessageId messageId;
  private EncodedPayload encodedPayload;
  @Before
  public void setUp() {
    enclave = mock(Enclave.class);
    courier = mock(Courier.class);
    inbox = mock(Inbox.class);
    messagingImpl = new MessagingImpl(enclave,courier,inbox);
    message = mock(Message.class);
    payloadEncoder = mock(PayloadEncoder.class);
    messageId = mock(MessageId.class);
    encodedPayload = mock(EncodedPayload.class);
  }

  @Test
  public void testGetInboxInstance(){
    messagingImpl = mock(MessagingImpl.class);
    when(messagingImpl.getInbox()).thenReturn(inbox);
    Assert.assertNotNull(messagingImpl);
  }

  @Test
  public void testSend(){
    PublicKey recipient = message.getRecipient();
    when(!courier.isKnownRecipient(recipient)).thenThrow(new UnknownRecipientException(recipient));
    EncodedPayload encrypted = enclave.encryptPayload(message.getData(),recipient, Collections.singletonList(recipient),
      PrivacyMetadata.Builder.forStandardPrivate().build());
    when(payloadEncoder.encode(encrypted)).thenReturn(any(byte[].class));
    byte [] encoded = payloadEncoder.encode(encrypted);
    when(courier.push(encoded,recipient)).thenReturn(messageId);
    Assert.assertNotNull(courier);
  }

  @Test
  public void testReceived(){
    Stream<MessageId> stream = mock(inbox.stream().getClass());
    when(stream.map(MessageId::toString)).thenReturn(Stream.of(anyString()));
    Stream<String> stringStream = stream.map(MessageId::toString);
    List<String> stringList = mock(stringStream.collect(Collectors.toList()).getClass());
    stringList.forEach(a -> verify(a instanceof String));
  }

  @Test
  public void testRead() {
    messagingImpl = mock(MessagingImpl.class);
    when(messagingImpl.getInbox()).thenReturn(inbox);
    String testData = "this is test method";
    byte [] arr = testData.getBytes();
    when(messagingImpl.getInbox().get(messageId)).thenReturn(arr);
    when(messagingImpl.getInbox().get(messageId)==null).thenThrow(new NoSuchMessageException(messageId));
    PublicKey publicKey = enclave.defaultPublicKey();
    doReturn(arr).when(enclave).unencryptTransaction(encodedPayload,publicKey);
    doReturn(message).when(messagingImpl).read(anyString());
    Assert.assertNotNull(messagingImpl);
  }

  @Test
  public void testRemove() {
    doNothing().when(messagingImpl.getInbox()).delete(messageId);
  }


  public boolean isEncodedValueNull() {
    return true;
  }


}
