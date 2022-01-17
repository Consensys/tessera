package com.quorum.tessera.messaging;

import com.quorum.tessera.base64.Base64Codec;
import com.quorum.tessera.encryption.PublicKey;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageTest extends TestCase {
  private PublicKey sender;
  private PublicKey recipient;
  private byte[] data;
  private Message message;
  private static final Base64Codec base64Codec = Base64Codec.create();

  @Before
  public void setUp() throws Exception {
    sender = mock(PublicKey.class);
    recipient = mock(PublicKey.class);
    //data = mock(byte[].class);
    message = mock(Message.class);
  }

  @Test
  public void testGetSender() {
    when(message.getSender()).thenReturn(sender);
  }

  @Test
  public void testGetRecipient() {
    when(message.getRecipient()).thenReturn(recipient);
  }

  @Test
  public void testGetData() {
    when(message.getData()).thenReturn(any());
  }

  @Test
  public void getToString() {
    when(message.toString()).thenReturn(getStringForReturn());
  }

  public String getStringForReturn() {
      return "[\""
        + sender.encodeToBase64()
        + "\" -> \""
        + recipient.encodeToBase64()
        + "\": \""
        + base64Codec.encodeToString(any())
        + "\"]";
  }

}
