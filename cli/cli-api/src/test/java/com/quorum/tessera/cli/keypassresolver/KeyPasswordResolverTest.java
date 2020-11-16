package com.quorum.tessera.cli.keypassresolver;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyPasswordResolverTest {

    @Test
    public void create() {
        assertThat(KeyPasswordResolver.create())
            .isNotNull()
            .isExactlyInstanceOf(CliKeyPasswordResolver.class);
    }

}
