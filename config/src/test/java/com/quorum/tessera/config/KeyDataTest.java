package com.quorum.tessera.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyDataTest {

    @Test
    public void setPassword() {
        char[] password = "password".toCharArray();
        KeyData keyData = new KeyData();
        keyData.setPassword(password);
        assertThat(password).isEqualTo(password);
    }
}
