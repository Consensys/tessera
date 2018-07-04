package com.github.nexus.config.cli;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class CliDelegateTest {

    private CliDelegate cliDelegate;

    public CliDelegateTest() {
    }

    @Before
    public void setUp() {
        cliDelegate = CliDelegate.instance();
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get("/tmp/anotherPrivateKey.key").toAbsolutePath());
        Files.deleteIfExists(Paths.get("/tmp/anotherPublicKey.key").toAbsolutePath());
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

    @Test
    public void withKeygenMissingKeyPaths() throws Exception {

        try {
            cliDelegate.execute(
                    "-keygen",
                    "-configfile",
                    getClass().getResource("/sample-config.json").getFile());

            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).hasSize(1);

            List<String> paths = ex.getConstraintViolations().stream()
                    .map(ConstraintViolation::getPropertyPath)
                    .map(Objects::toString)
                    .sorted()
                    .collect(Collectors.toList());

            assertThat(paths).containsExactly("keys[0].privateKey.path");
        }
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

    @Test
    public void keygen() throws Exception {

        CliResult result = cliDelegate.execute(
                "-keygen",
                "-configfile",
                getClass().getResource("/keygen-sample.json").getFile());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(0);

        assertThat(result.getConfig()).isNotNull();

    }

}
