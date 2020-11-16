package com.quorum.tessera.config.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NoTesseraConfigfileOptionExceptionTest {

    @Test
    public void testDefaultConstrcutor() {
        assertThat(new NoTesseraConfigfileOptionException()).isNotNull();
    }

}
