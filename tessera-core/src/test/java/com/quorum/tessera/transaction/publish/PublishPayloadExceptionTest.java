package com.quorum.tessera.transaction.publish;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PublishPayloadExceptionTest {

    @Test
    public void createWithMessage() {
        final String msg = "msg";
        PublishPayloadException exception = new PublishPayloadException(msg);

        assertThat(exception).hasMessage(msg);
    }

}
