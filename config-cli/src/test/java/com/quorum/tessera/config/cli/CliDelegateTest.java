package com.quorum.tessera.config.cli;

import com.quorum.tessera.test.util.ElUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.validation.ConstraintViolationException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class CliDelegateTest {

    private final CliDelegate instance = CliDelegate.INSTANCE;

    @Test
    public void createInstance() {

        assertThat(CliDelegate.instance()).isSameAs(instance);

    }

    @Test
    public void withValidConfig() throws Exception {

        Path configFile = ElUtil.createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        CliResult result = instance.execute(
                "-configfile",
                configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig().get()).isSameAs(instance.getConfig());
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isHelpOn()).isFalse();
        assertThat(result.isKeyGenOn()).isFalse();
    }

    @Test
    public void withEmptyConfigOverrideAll() throws Exception {

        Path unixSocketFile = Files.createTempFile("unixSocketFile", ".ipc");
        unixSocketFile.toFile().deleteOnExit();

        Path configFile = Files.createTempFile("withEmptyConfigOverrideAll", ".json");
        configFile.toFile().deleteOnExit();
        Files.write(configFile, "{}".getBytes());
        try {
            CliResult result = instance.execute(
                    "-configfile",
                    configFile.toString(),
                    "--unixSocketFile",
                    unixSocketFile.toString()
            );

            assertThat(result).isNotNull();
        } catch (ConstraintViolationException ex) {
            ex.getConstraintViolations().forEach(System.out::println);
        }
    }

}
