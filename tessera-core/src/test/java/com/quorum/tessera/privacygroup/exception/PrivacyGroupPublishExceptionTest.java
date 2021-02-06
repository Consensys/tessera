package com.quorum.tessera.privacygroup.exception;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivacyGroupPublishExceptionTest {

    @Test
    public void createInstance() {
        PrivacyGroupPublishException ex = new PrivacyGroupPublishException("OUCH");
        assertThat(ex).isNotNull();
    }
}
