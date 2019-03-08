package com.quorum.tessera.config.cli;

import com.quorum.tessera.test.util.ElUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.validation.ConstraintViolationException;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.Fail;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliDelegateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CliDelegateTest.class);
    
    private final CliDelegate instance = CliDelegate.INSTANCE;

    @Test
    public void createInstance() {

        assertThat(CliDelegate.instance()).isSameAs(instance);

    }

    @Test
    public void createAdminInstance() throws Exception {

        Path configFile = ElUtil.createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        CliResult result = instance.execute("admin", "-configfile", configFile.toString());

        assertThat(result).isNotNull();
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
        assertThat(result.isSuppressStartup()).isFalse();
 
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
            Fail.failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            ex.getConstraintViolations().forEach(v -> LOGGER.info("{}",v));
        }
    }
    
    @Test(expected = IllegalStateException.class)
    public void fetchWithoutExecution() {
        instance.getConfig();
    }

}
