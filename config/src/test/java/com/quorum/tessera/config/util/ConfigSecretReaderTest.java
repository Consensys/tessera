package com.quorum.tessera.config.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigSecretReaderTest {

    private String filePath;

    @Rule
    public final org.junit.contrib.java.lang.system.EnvironmentVariables envVariables = new EnvironmentVariables();

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Before
    public void setUp() {
        filePath = getClass().getResource("/key.secret").getPath();
    }

    @Test
    public void testReadSecret() {

        envVariables.set(com.quorum.tessera.config.util.EnvironmentVariables.CONFIG_SECRET_PATH, filePath);

        Optional<String> secret = ConfigSecretReader.readSecretFromFile();

        assertThat(secret).isPresent();
        assertThat(secret.get()).isEqualTo("quorum");
    }

    @Test
    public void testNotAbleToReadSecret() {

        envVariables.set(com.quorum.tessera.config.util.EnvironmentVariables.CONFIG_SECRET_PATH, "not-existed");
        assertThat(ConfigSecretReader.readSecretFromFile()).isEmpty();
    }

    @Test
    public void envNotSet() {
        envVariables.clear(com.quorum.tessera.config.util.EnvironmentVariables.CONFIG_SECRET_PATH);
        assertThat(ConfigSecretReader.readSecretFromFile()).isEmpty();
    }

    @Test
    public void testReadFromConsole() {
        ByteArrayInputStream in = new ByteArrayInputStream("password".getBytes());
        System.setIn(in);
        assertThat(ConfigSecretReader.readSecretFromConsole()).isEqualTo("password");

        System.setIn(System.in);
        assertThat(ConfigSecretReader.readSecretFromConsole()).isEqualTo("");
    }

    @Test
    public void testReadException() throws IOException {
        envVariables.clear(com.quorum.tessera.config.util.EnvironmentVariables.CONFIG_SECRET_PATH);

        final File tempFile = tempDir.newFile("key.secret");
        tempFile.setReadable(false);


        envVariables.set(com.quorum.tessera.config.util.EnvironmentVariables.CONFIG_SECRET_PATH,
            tempFile.getAbsolutePath());

        assertThat(ConfigSecretReader.readSecretFromFile()).isEmpty();


    }
}
