package com.quorum.tessera.cli;

import com.quorum.tessera.test.util.ElUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class CliDelegateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CliDelegateTest.class);

    private final CliDelegate instance = CliDelegate.INSTANCE;

    @Test
    public void createInstance() {
        assertThat(CliDelegate.instance()).isSameAs(instance);
    }

    @Test
    public void adminCliOptionCreatesAdminInstance() throws Exception {
        Path configFile = ElUtil.createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        CliResult result = instance.execute("admin", "-configfile", configFile.toString());

        assertThat(result).isNotNull();
        // MockAdminCliAdapter should be loaded by ServiceLoader and returns status 101
        assertThat(result.getStatus()).isEqualTo(101);
    }

    @Test
    public void standardCliOptionsCreatesConfigInstance() throws Exception {
        Path configFile = ElUtil.createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        CliResult result = instance.execute("-configfile", configFile.toString());

        assertThat(result).isNotNull();
        // MockDefaultCliAdapter should be loaded by ServiceLoader and returns status 100
        assertThat(result.getStatus()).isEqualTo(100);
    }

    @Test(expected = IllegalStateException.class)
    public void fetchWithoutExecution() {
        instance.getConfig();
    }
}
