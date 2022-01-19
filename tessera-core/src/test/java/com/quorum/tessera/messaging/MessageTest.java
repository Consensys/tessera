package com.quorum.tessera.messaging;

import com.quorum.tessera.base64.Base64Codec;
import com.quorum.tessera.encryption.PublicKey;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageTest{
  private PublicKey sender;
  private PublicKey recipient;
  private byte[] data;
  private Message message;
  private static final Base64Codec base64Codec = Base64Codec.create();

  @Before
  public void setUp()  {
    sender = mock(PublicKey.class);
    recipient = mock(PublicKey.class);
    data = new byte[256];
    message = mock(Message.class);
  }

  @Test
  public void testGetSender() {
    PublicKey key = sender;
    when(message.getSender()).thenReturn(key);
  }

  @Test
  public void testGetRecipient() {
    PublicKey key = recipient;
    when(message.getRecipient()).thenReturn(key);
  }

  @Test
  public void testGetData() {
    when(message.getData()).thenReturn(data);
  }
}
