package com.quorum.tessera.messaging;

import com.quorum.tessera.encryption.PublicKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class UnknownRecipientExceptionTest {

  private PublicKey publicKey;
  private UnknownRecipientException unknownRecipientException;

  @Before
  public void setUp() {
    publicKey = mock(PublicKey.class);
    unknownRecipientException = mock(UnknownRecipientException.class);
  }

  @Test
  public void testGetPublicKey(){
    UnknownRecipientException unknownRecipientException = new UnknownRecipientException(publicKey);
    Assert.assertNotNull(unknownRecipientException);
    Assert.assertNotNull(unknownRecipientException.getPublicKey());
  }

  @Test
  public void testUnknownRecipientExceptionWithArgs(){
    UnknownRecipientException unknownRecipientException = new UnknownRecipientException(publicKey);
    Assert.assertNotNull(unknownRecipientException);
  }
}
