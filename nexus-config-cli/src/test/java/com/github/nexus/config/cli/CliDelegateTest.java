package com.github.nexus.config.cli;

import com.github.nexus.config.Config;
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
    public void callApiVersionWithValidConfig() throws Exception {

        Config result = cliDelegate.execute(
                "-configfile",
                getClass().getResource("/sample-config.json").getFile(),
                "-version");

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(cliDelegate.getConfig());
    }

    @Test(expected = FileNotFoundException.class)
    public void callApiVersionWithConfigFileDoesnotExist() throws Exception {
        cliDelegate.execute("-configfile", "bogus.json", "-version");
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
                    getClass().getResource("/missing-config.json").getFile(),
                    "-version");
                    
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
               assertThat(ex.getConstraintViolations()).hasSize(1);
        }

    }
}
