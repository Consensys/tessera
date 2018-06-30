package com.github.nexus.config.cli;

import java.io.FileNotFoundException;
import javax.validation.ConstraintViolationException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;

public class CliDelegateTest {

    private CliDelegate cliDelegate;

    public CliDelegateTest() {
    }

    @Before
    public void setUp() {
        cliDelegate = CliDelegate.instance();
    }

    @Test
    public void help() throws Exception {

        CliResult result = cliDelegate.execute("help");
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);

    }

    @Test
    public void withValidConfig() throws Exception {

        CliResult result = cliDelegate.execute(
                "-configfile",
                getClass().getResource("/sample-config.json").getFile());

        assertThat(result).isNotNull();
        assertThat(result.getConfig().get()).isSameAs(cliDelegate.getConfig());
        assertThat(result.getStatus()).isEqualTo(0);
    }

    //@Test
    public void withValidConfigAndKeygen() throws Exception {

        cliDelegate.execute(
                "-keygen",
                "-configfile",
                getClass().getResource("/sample-config.json").getFile());

    }

    @Test(expected = FileNotFoundException.class)
    public void callApiVersionWithConfigFileDoesnotExist() throws Exception {
        cliDelegate.execute("-configfile", "bogus.json");
    }

    @Test(expected = CliException.class)
    public void processArgsMissing() throws Exception {
        cliDelegate.execute();
    }

    @Test
    public void withConstraintViolations() throws Exception {

        try {
            cliDelegate.execute(
                    "-configfile",
                    getClass().getResource("/missing-config.json").getFile());
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).hasSize(1);
        }

    }


}
