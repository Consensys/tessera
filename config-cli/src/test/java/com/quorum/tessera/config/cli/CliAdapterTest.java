package com.quorum.tessera.config.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CliAdapterTest {

    @Test
    public void createDefault() throws Exception {

        CliAdapter result = CliAdapter.create();

        assertThat(result).isExactlyInstanceOf(DefaultCliAdapter.class);

    }
}
