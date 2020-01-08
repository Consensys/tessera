package com.quorum.tessera.key.vault.aws;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IncompleteAWSCredentialsExceptionTest {

    @Test
    public void createWithMessage() {
        final String msg = "msg";
        IncompleteAWSCredentialsException exception = new IncompleteAWSCredentialsException(msg);

        assertThat(exception).hasMessage(msg);
    }
}
