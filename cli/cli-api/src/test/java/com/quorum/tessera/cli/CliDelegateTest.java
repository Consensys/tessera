package com.quorum.tessera.cli;

import com.quorum.tessera.config.Config;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class CliDelegateTest {

    private final CliDelegate instance = CliDelegate.INSTANCE;

    @Test
    public void createInstance() {
        assertThat(CliDelegate.instance()).isSameAs(instance);
    }

    @Test
    public void adminCliOptionCreatesAdminInstance() throws Exception {
        MockCliAdapter.setType(CliType.ADMIN);
        int status = 111;
        MockCliAdapter.setResult(new CliResult(status, true, null));

        CliResult result = instance.execute("admin", "-configfile", "/path/to/file");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(status);
    }

    @Test
    public void standardCliOptionsCreatesConfigInstance() throws Exception {
        MockCliAdapter.setType(CliType.CONFIG);
        int status = 111;
        Config config = new Config();
        MockCliAdapter.setResult(new CliResult(status, true, config));

        CliResult result = instance.execute("-configfile", "path/to/file");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(status);
        assertThat(result.getConfig().get()).isEqualTo(config);
    }

    @Test
    public void configFieldUpdatedAfterExecution() throws Exception {
        MockCliAdapter.setType(CliType.CONFIG);
        int status = 111;
        Config config = new Config();
        MockCliAdapter.setResult(new CliResult(status, false, config));

        instance.execute("-configfile", "/path/to/file");

        assertThat(instance.getConfig()).isEqualTo(new Config());
    }

    @Test(expected = IllegalStateException.class)
    public void fetchConfigWithoutExecution() {
        instance.getConfig();
    }

    // PicoCLI tests
    @Test
    public void nullResultReturnsNull() {
        assertThat(instance.getResult(null)).isNull();
    }

    @Test
    public void nonnullResultReturnsResult() throws Exception {
        final CliResult result = new CliResult(0, true, null);
        MockSubcommandCliAdapter.setResult(result);

        assertThat(instance.execute("some-subcommand")).isSameAs(result);
    }

    @Test
    public void exceptionFromCommandBubblesUp() {
        final Exception exception = new Exception();
        MockSubcommandCliAdapter.setExceptionToBeThrown(exception);

        final Throwable throwable = catchThrowable(() -> instance.execute("some-subcommand"));

        assertThat(throwable).isSameAs(exception);

        MockSubcommandCliAdapter.setExceptionToBeThrown(null);
    }
}
