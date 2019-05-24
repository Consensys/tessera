package com.quorum.tessera.cli;

import com.quorum.tessera.config.Config;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class CliDelegateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CliDelegateTest.class);

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
    public void adminCliOptionButNoAdminCliAvailableThrowsException() throws Exception {
        MockCliAdapter.setType(CliType.CONFIG);

        Throwable ex = catchThrowable(() -> instance.execute("admin"));

        assertThat(ex).isNotNull();
        assertThat(ex).isExactlyInstanceOf(CliException.class);
        assertThat(ex).hasMessage("No valid implementation of CliAdapter found on the classpath");
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
    public void standardCliOptionsButNoNonAdminCliAvailableThrowsException() throws Exception {
        MockCliAdapter.setType(CliType.ADMIN);

        Throwable ex = catchThrowable(() -> instance.execute("-configfile", "/path/to/file"));

        assertThat(ex).isNotNull();
        assertThat(ex).isExactlyInstanceOf(CliException.class);
        assertThat(ex).hasMessage("No valid implementation of CliAdapter found on the classpath");
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
}
