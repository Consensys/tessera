package com.quorum.tessera.messaging;

import com.quorum.tessera.data.EncryptedMessageDAO;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.Mockito.*;

public class InboxTest {
  private Inbox inbox;
  private MessageId messageId;
  private  ServiceLoader serviceLoader;
  @Before
  public void setUp() {
    inbox = mock(Inbox.class);
    messageId = mock(MessageId.class);
    serviceLoader = mock(ServiceLoader.class);
  }

  @Test
  public void testPut() {
    String testData = "This is the test data";
    byte[] data = testData.getBytes();
    doReturn(messageId).when(inbox).put(data);
  }
  @Test
  public void testGet(){
    String testData = "This is the test data";
    byte[] data = testData.getBytes();
    doReturn(data).when(inbox).get(messageId);
  }
  @Test
  public void testStream(){
    when(inbox.stream()).thenReturn(Stream.of(messageId));
  }

  @Test
  public void testDelete() {
    doNothing().when(inbox).delete(messageId);
  }

  @Test
  public void testCreate() {
    when(serviceLoader.findFirst()).thenReturn(Optional.of(inbox));
    when(serviceLoader.findFirst().get()).thenReturn(Optional.of(inbox));
    doReturn(Optional.ofNullable(Inbox.class)).when(serviceLoader).findFirst();
  }
}
