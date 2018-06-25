package com.github.nexus.ssl.strategy;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class AuthenticationModeTest {

    @Test
    public void testAuthenticationOn(){
        assertThat(AuthenticationMode.getValue("strict"))
            .isEqualByComparingTo(AuthenticationMode.strict);
    }

    @Test
    public void testAuthenticationOff(){
        assertThat(AuthenticationMode.getValue("off"))
            .isEqualByComparingTo(AuthenticationMode.off);
    }

    @Test
    public void testAuthenticationOffForInvalidValue(){
        assertThat(AuthenticationMode.getValue("something"))
            .isEqualByComparingTo(AuthenticationMode.off);
    }
}
