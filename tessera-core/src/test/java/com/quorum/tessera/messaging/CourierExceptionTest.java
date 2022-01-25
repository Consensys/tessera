package com.quorum.tessera.messaging;

import org.junit.Assert;
import org.junit.Test;

public class CourierExceptionTest {

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
