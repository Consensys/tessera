package com.quorum.tessera.privacygroup.exception;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivacyGroupNotFoundExceptionTest {

    @Test
    public void createInstance() {

        PrivacyGroupNotFoundException ex = new PrivacyGroupNotFoundException("not found");
        assertThat(ex).isNotNull();
    }
}
