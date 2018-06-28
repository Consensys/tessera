package com.github.nexus.config.cli;

import com.github.nexus.config.Config;
import java.io.FileNotFoundException;
import javax.validation.ConstraintViolationException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class CliDelegateTest {

    private CliDelegate cliDelegate;

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    

    public CliDelegateTest() {
    }

    @Before
    public void setUp() {

        
        
        cliDelegate = CliDelegate.instance();
    }

    @Test
    public void help() throws Exception {
        exit.expectSystemExitWithStatus(0);
        cliDelegate.execute("help");


    }

    @Test
    public void withValidConfig() throws Exception {

        Config result = cliDelegate.execute(
                "-configfile",
                getClass().getResource("/sample-config.json").getFile());

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(cliDelegate.getConfig());
    }

    @Test
    public void withValidConfigAndKeygen() throws Exception {
        exit.expectSystemExitWithStatus(0);
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
