package com.quorum.tessera.messaging.internal;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.quorum.tessera.data.*;
import com.quorum.tessera.messaging.MessageId;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class InboxImplTest {
  private InboxImpl inbox;
  private final EncryptedMessageDAO dao = mock(EncryptedMessageDAO.class);
  private MessageHash messagingHash;
  byte[] dataForHashConversion = null;
  private MessageDigest sha256;
  private MessageId messageId;
  Logger LOGGER ;
  @Before
  public void setUp() {
    inbox = new InboxImpl(dao);
    messagingHash = mock(MessageHash.class);
    String testData = "This is the test data for for Checking SHA-256 and Truncation";
    dataForHashConversion = testData.getBytes();
    sha256 = mock(MessageDigest.class);
    messageId = mock(MessageId.class);
    LOGGER = mock(Logger.class);
  }

  @Test
  public void testConstructorWithArg() {
    InboxImpl inbox = new InboxImpl(dao);
    Assert.assertNotNull(inbox);

    new InboxImpl(dao);

  }

  @Test(expected = IllegalStateException.class)
  public void testConstructorWithNoSuchAlgorithmException()   {
    MockedStatic<MessageDigest> instance = Mockito.mockStatic(MessageDigest.class);
    try {
    instance.when(() -> MessageDigest.getInstance("SHA-256")).thenThrow(new NoSuchAlgorithmException());
    InboxImpl impl = new InboxImpl(dao);
  }finally {
    instance.close();
  }
  }

  @Test
  public void testHash() {
    MessageHash messageHash = inbox.hash(dataForHashConversion);
    assertEquals(messageHash.getHashBytes().length, 20);

    Mockito.doNothing().when(sha256).reset();
    assertNotNull(sha256);
    when(messagingHash.getHashBytes()).thenReturn("test".getBytes());
    assertEquals(4,messagingHash.getHashBytes().length);
    assertNotNull(messagingHash);

  }

  @Test
  public void testPut() {
    String testMessage = "this is simple put test";
    MessageHash messageHash = new MessageHash(testMessage.getBytes());
    EncryptedMessage encryptedMessage = new EncryptedMessage(messageHash, "ok".getBytes());
    Assert.assertNotNull(encryptedMessage);
    doReturn(encryptedMessage).when(dao).save(any());
    InboxImpl impl  = new InboxImpl(dao);
    impl.put("ok".getBytes());
    assertNotNull(dao.save(encryptedMessage));

  }

  @Test
  public void testGet() {
    String testMsg = "Hi";
    MessageId messageId = new MessageId(testMsg.getBytes());
    MessageHash messageHash = new MessageHash(messageId.getValue());
    EncryptedMessage encryptedMessage = new EncryptedMessage(messageHash, testMsg.getBytes());
    when(dao.retrieveByHash(messageHash)).thenReturn(Optional.ofNullable(Optional.of(encryptedMessage).orElse(null)));

    InboxImpl impl = new InboxImpl(dao);
    assertNotNull(impl.get(messageId).length);
    assertTrue(impl.get(messageId).length > 0);

  }

  @Test
  public void testStream() {
    Stream<MessageHash> stream = mock(dao.retrieveMessageHashes(0, Integer.MAX_VALUE).stream().getClass());
    Stream<byte[]> byteStream = stream.map(MessageHash::getHashBytes);
    Stream<MessageId> stringStream = byteStream.map(MessageId::new);
    List<MessageId> messageIdList = mock(stringStream.collect(Collectors.toList()).getClass());
    messageIdList.forEach(a -> verify(a instanceof MessageId));

    InboxImpl impl = new InboxImpl(dao);
    impl.stream();

    assertNotNull(dao.retrieveMessageHashes(0, Integer.MAX_VALUE).stream()
      .map(MessageHash::getHashBytes)
      .map(MessageId::new));

  }


  @Test
  public void testDelete() {

    String test = "data";
    MessageId messageid = new MessageId(test.getBytes());
    assertNotNull(messageid);
    assertNotNull(messageid.getValue());
    doReturn("".getBytes()).when(messageId).getValue();
    final MessageHash hash = new MessageHash("I LOVE SPARROWS".getBytes());
    assertThat(hash).isEqualTo(hash).hasSameHashCodeAs(hash);
    doNothing().when(dao).delete(hash);

    Assertions.assertThat(Arrays.equals("I LOVE SPARROWS".getBytes(), hash.getHashBytes())).isTrue();

    InboxImpl impl = new InboxImpl(dao);
    impl.delete(messageid);
  }
  }


