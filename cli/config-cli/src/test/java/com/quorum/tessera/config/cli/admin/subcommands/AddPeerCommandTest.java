package com.quorum.tessera.config.cli.admin.subcommands;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.parsers.ConfigurationMixin;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.util.ConfigFileStore;
import com.quorum.tessera.test.util.ElUtil;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Invocation;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class AddPeerCommandTest {

    private AddPeerCommand command;

    private Invocation.Builder invocationBuilder;

    @Before
    public void onSetUp() {

        command = new AddPeerCommand();
    }

    @Test
    public void nullParametersReturnsFalse() {
        // First go
        command.setConfigMixin(null);
        final CliResult resultOne = command.call();
        assertThat(resultOne).isEqualToComparingFieldByField(new CliResult(1, true, null));

        // Second go
        final ConfigurationMixin configurationMixin = new ConfigurationMixin();
        command.setConfigMixin(configurationMixin);
        final CliResult resultTwo = command.call();
        assertThat(resultTwo).isEqualToComparingFieldByField(new CliResult(1, true, null));

        // Third go
        final Config config = new Config();
        configurationMixin.setConfig(config);
        final CliResult resultThree = command.call();
        assertThat(resultThree).isEqualToComparingFieldByField(new CliResult(1, true, null));
    }

    @Test
    public void addPeer() throws Exception {

        Path resultFile = Files.createTempFile("addPeer","txt");
        resultFile.toFile().deleteOnExit();

        ConfigFileStore.create(resultFile);

        final Path configFile = ElUtil.createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        final ConfigurationMixin mixin = new ConfigurationMixin();
        try (InputStream in = Files.newInputStream(configFile)) {
            final Config config = ConfigFactory.create().create(in);
            mixin.setConfig(config);
        }

        command.setConfigMixin(mixin);
        command.setPeerUrl("http://junit.com:8989");

        final CliResult result = command.call();
        assertThat(result).isEqualToComparingFieldByField(new CliResult(0, true, mixin.getConfig()));

    }

}
