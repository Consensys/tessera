package com.quorum.tessera.messaging;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class MessageIdTest {
  MessageId messageId;

  @Before
  public void setUp() {
    messageId = mock(MessageId.class);
  }

  @Test
  public void testParseMessageId() {
    //  final int length = (stringValue == null) ? 0 : stringValue.length();
   // String test = Mockito.spy(String.class);
    /*when(anyString()).thenAnswer((Answer<Integer>) invocation -> {
      if(test != null)
        return test.length();
      else
        return 0;
    });
    */
    /*doAnswer(new Answer<Integer>() {
      @Override
      public Integer answer(InvocationOnMock invocation) throws Throwable {
        if(test != null)
          return test.length();
        else
          return 0;
      }
    }).when(test);

     */
    // given(anyString()).willReturn(String.valueOf(0));
  }

  @Test
  public void testGetValue() {
    byte[] value = new byte[256];
    when(messageId.getValue()).thenReturn(value);
  }
}
