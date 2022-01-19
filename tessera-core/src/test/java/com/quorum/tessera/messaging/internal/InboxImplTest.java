package com.quorum.tessera.messaging.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.EncryptedMessage;
import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.data.MessageHash;

import com.quorum.tessera.messaging.MessageId;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InboxImplTest {
  private InboxImpl inbox;
  private EncryptedMessageDAO dao;
  private MessageHash messageHash;
  byte[] dataForHashConversion = null;
  private EncryptedMessage encryptedMessage;
  private MessageId messageId;

  @Before
  public void setUp() {
    dao = mock(EncryptedMessageDAO.class);
    inbox = new InboxImpl(dao);
    messageHash = mock(MessageHash.class);
    String testData = "This is the test data for for Checking SHA-256 and Truncation";
    dataForHashConversion = testData.getBytes();
  }

  @Test
  public void testHash() {
    MessageHash messageHash = inbox.hash(dataForHashConversion);
    assertEquals(messageHash.getHashBytes().length, 20);
  }

  @Test
  public void testPut() {
    String testMessage = "this is simple put test";
    MessageHash messageHash = mock(MessageHash.class);
    EncryptedMessage encryptedMessage = new EncryptedMessage(messageHash, testMessage.getBytes());
//when(dao.save(any())).thenReturn(encryptedMessage);
    doReturn(encryptedMessage).when(dao).save(any());
  }

  @Test
  public void testGet() {
    String testMsg = "Hi";
    MessageId messageId = new MessageId(testMsg.getBytes());
    MessageHash messageHash = new MessageHash(messageId.getValue());
    EncryptedMessage encryptedMessage = new EncryptedMessage(messageHash, testMsg.getBytes());
    when(dao.retrieveByHash(messageHash)).thenReturn(Optional.ofNullable(Optional.of(encryptedMessage).orElse(null)));
  }

  @Test
  public void testStream() {
    Stream<MessageHash> stream = mock(dao.retrieveMessageHashes(0, Integer.MAX_VALUE).stream().getClass());
    Stream<byte[]> byteStream = stream.map(MessageHash::getHashBytes);
    Stream<MessageId> stringStream = byteStream.map(MessageId::new);
    List<MessageId> messageIdList = mock(stringStream.collect(Collectors.toList()).getClass());
    messageIdList.forEach(a -> verify(a instanceof MessageId));
  }

  @Test
  public void testDelete() {
    MessageHash messageHash = mock(MessageHash.class);
    doNothing().when(dao).delete(messageHash);
  }

}
