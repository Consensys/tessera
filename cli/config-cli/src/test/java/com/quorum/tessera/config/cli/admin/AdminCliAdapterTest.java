package com.quorum.tessera.config.cli.admin;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminCliAdapterTest {

    private AdminCliAdapter adminCliAdapter = new AdminCliAdapter();

    @Test
    public void getType() {
        assertThat(adminCliAdapter.getType()).isEqualTo(CliType.ADMIN);
    }

    @Test
    public void callIsSuccessful() {
        final CliResult result = adminCliAdapter.call();

        assertThat(result.getConfig()).isEmpty();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isTrue();
    }

    @Test
    public void successfulRunGivesZeroExitCode() {
        final CliResult result = this.adminCliAdapter.execute();

        assertThat(result.getConfig()).isEmpty();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isTrue();
    }
}
