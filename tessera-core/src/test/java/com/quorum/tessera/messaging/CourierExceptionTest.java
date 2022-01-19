package com.quorum.tessera.messaging;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CourierExceptionTest {
  @Before
  public void setUp(){

  }

  @Test
  public void testCourierExceptionWithMessageArgument() {
   CourierException courierException = new CourierException("");
    Assert.assertNotNull(courierException);
  }

  @Test
  public void testCourierExceptionWithTwoArguments() {
    CourierException courierException = new CourierException("",new Exception());
    Assert.assertNotNull(courierException);
  }
}
