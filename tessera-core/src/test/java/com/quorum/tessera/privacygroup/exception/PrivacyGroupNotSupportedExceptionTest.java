package com.quorum.tessera.privacygroup.exception;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivacyGroupNotSupportedExceptionTest {

    @Test
    public void createInstance() {
        PrivacyGroupNotSupportedException ex = new PrivacyGroupNotSupportedException("OUCH");
        assertThat(ex).isNotNull();
    }
}
