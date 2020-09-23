package com.quorum.tessera.transaction.publish;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncPublishPayloadExceptionTest {

    @Test
    public void constructor() {
        final RuntimeException root = new RuntimeException("root cause");
        AsyncPublishPayloadException exception = new AsyncPublishPayloadException(root);

        assertThat(exception).hasCause(root);
    }

}
