package com.quorum.tessera.threading;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CountDownLatchCancelledExceptionTest {

    @Test
    public void constructor() {
        CountDownLatchCancelledException ex = new CountDownLatchCancelledException();
        assertThat(ex).isNotNull();
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

}
