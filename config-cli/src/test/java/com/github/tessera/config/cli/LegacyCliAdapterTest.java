package com.github.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class LegacyCliAdapterTest {

    private final LegacyCliAdapter instance = new LegacyCliAdapter();

    @Test
    public void help() throws Exception {

        CliResult result = instance.execute("--help");
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);

    }

    @Test
    public void noOptions() throws Exception {

        CliResult result = instance.execute();
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(1);

    }
}
