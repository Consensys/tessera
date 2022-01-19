package com.quorum.tessera.messaging;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ServiceLoader;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class MessagingTest {
  private Messaging messaging;
  private Message message;
  private ServiceLoader serviceLoader;

  @Before
  public void setUp() {
   message = mock(Message.class);
   messaging = mock(Messaging.class);
   serviceLoader = mock(ServiceLoader.class);
  }

  @Test
  public void testSend(){
   // when(messaging.send(message)).thenReturn("");
    Assert.assertNotNull(messaging);
  }

  @Test
  public void testReceived(){
    when(messaging.received()).thenReturn(new ArrayList<>());
  }

  @Test
  public void testRead() throws NoSuchMessageException {
    when(messaging.read(anyString())).thenReturn(message);
  }

  @Test
  public void testRemove() {
    doNothing().when(messaging).remove(anyString());
  }

  @Test
  public void testCreate() {
    when(serviceLoader.findFirst()).thenReturn(Optional.of(messaging));
    when(serviceLoader.findFirst().get()).thenReturn(Optional.of(messaging));
    doReturn(Optional.ofNullable(Messaging.class)).when(serviceLoader).findFirst();
  }
}
