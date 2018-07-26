package com.quorum.tessera.config.cli;

import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CliAdapterTest {

    @After
    public void onTearDown() {
        System.clearProperty("tessera.config.legacy");
    }

    @Test
    public void createLegacy() throws Exception {
        System.setProperty("tessera.config.legacy", "true");
        CliAdapter result = CliAdapter.create();
        assertThat(result).isExactlyInstanceOf(LegacyCliAdapter.class);
    }

    @Test
    public void createDefault() throws Exception {

        CliAdapter result = CliAdapter.create();

        assertThat(result).isExactlyInstanceOf(DefaultCliAdapter.class);

    }
}
