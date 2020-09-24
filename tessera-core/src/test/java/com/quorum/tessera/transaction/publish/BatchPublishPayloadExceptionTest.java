package com.quorum.tessera.transaction.publish;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchPublishPayloadExceptionTest {

    @Test
    public void constructor() {
        final RuntimeException root = new RuntimeException("root cause");
        BatchPublishPayloadException exception = new BatchPublishPayloadException(root);

        assertThat(exception).hasCause(root);
    }

}
