package com.quorum.tessera.cli;

import com.quorum.tessera.io.NoopSystemAdapter;
import com.quorum.tessera.io.SystemAdapter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CliAdapterTest {

    @Test
    public void sys() {
        CliAdapter cliAdapter = new MockCliAdapter();
        SystemAdapter systemAdapter = cliAdapter.sys();

        assertThat(systemAdapter).isExactlyInstanceOf(NoopSystemAdapter.class);
    }
}
