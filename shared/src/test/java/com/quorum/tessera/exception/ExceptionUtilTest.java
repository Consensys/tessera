package com.quorum.tessera.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ExceptionUtilTest {

    @Test
    public void extractWithNoCause() {
        
        Exception exception = new Exception();

        Throwable cause = ExceptionUtil.extractCause(exception);

        assertThat(cause).isSameAs(exception);
    }
    
    @Test
    public void extractWithCause() {
        Exception nestedException = new Exception();
        Exception exception = new Exception(nestedException);
     
        Throwable cause = ExceptionUtil.extractCause(exception);

        assertThat(cause).isSameAs(nestedException);
    }
}
