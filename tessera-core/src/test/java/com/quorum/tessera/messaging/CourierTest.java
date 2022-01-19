package com.quorum.tessera.messaging;

import com.quorum.tessera.encryption.PublicKey;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;
import java.util.ServiceLoader;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;


public class CourierTest  {
  private PublicKey publicKey;
  private Courier courier;
  private MessageId messageId;
  private ServiceLoader serviceLoader;
  @Before
  public void setUp(){
    publicKey = mock(PublicKey.class);
    courier = mock(Courier.class);
    messageId = mock(MessageId.class);
    serviceLoader = mock(ServiceLoader.class);
  }

  @Test
  public void testIsKnownRecipient() {
    when(courier.isKnownRecipient(publicKey)).thenReturn(true);
  }

  @Test
  public void testPush() {
    String testData = "this is for testing";
    byte[] data = testData.getBytes();
    when(courier.push(data,publicKey)).thenReturn(messageId);
  }

  @Test
  public void testCreate() {
    when(serviceLoader.findFirst()).thenReturn(Optional.of(courier));
    when(serviceLoader.findFirst().get()).thenReturn(Optional.of(courier));
    doReturn(Optional.ofNullable(Courier.class)).when(serviceLoader).findFirst();
  }
}
