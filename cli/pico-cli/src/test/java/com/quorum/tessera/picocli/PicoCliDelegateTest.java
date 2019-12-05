package com.quorum.tessera.picocli;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

import static com.quorum.tessera.test.util.ElUtil.createAndPopulatePaths;
import static org.assertj.core.api.Assertions.assertThat;

public class PicoCliDelegateTest {

    private PicoCliDelegate cliDelegate;

    @Before
    public void setUp() {
        cliDelegate = new PicoCliDelegate();
    }

    @Test
    public void help() throws Exception {

        final CliResult result = cliDelegate.execute("help");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isTrue();
    }

    @Test
    public void noArgsPrintsHelp() throws Exception {

        final CliResult result = cliDelegate.execute();

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isTrue();
    }

    @Test
    public void withValidConfig() throws Exception {

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        CliResult result = cliDelegate.execute("-configfile", configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isFalse();
    }

    @Test(expected = CliException.class)
    public void processArgsMissing() throws Exception {
        cliDelegate.execute("-configfile");
    }

}
