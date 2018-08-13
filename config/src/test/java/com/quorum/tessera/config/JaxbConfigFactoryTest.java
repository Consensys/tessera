package com.quorum.tessera.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static java.lang.System.lineSeparator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class JaxbConfigFactoryTest {

    private static final String PASS = "pass" + lineSeparator();

    private JaxbConfigFactory factory;

    private InputStream oldSystemIn;

    @Before
    public void init() {
        final InputStream tempSystemIn = new ByteArrayInputStream((PASS + PASS).getBytes());
        this.oldSystemIn = System.in;
        System.setIn(tempSystemIn);

        this.factory = new JaxbConfigFactory();
    }

    @After
    public void after() throws IOException {
        System.setIn(oldSystemIn);
        Files.deleteIfExists(Paths.get("newPasses.txt"));
        Files.deleteIfExists(Paths.get("passwords.txt"));
    }

    @Test
    public void createNewLockedKeyAddPasswordToInline() throws IOException {

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyAddInline.json");
        final Path blankName = Files.createTempDirectory(UUID.randomUUID().toString());

        final Config config = factory.create(inputStream, null, blankName.toAbsolutePath().toString());

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getPasswords()).hasSize(1).containsExactlyInAnyOrder("pass");
        assertThat(config.getKeys().getPasswordFile()).isNull();
    }

    @Test
    public void createNewLockedKeyAppendsToList() throws IOException {

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyAddInlineWithExisting.json");
        final Path blankName = Files.createTempDirectory(UUID.randomUUID().toString());

        final Config config = factory.create(inputStream, null, blankName.toAbsolutePath().toString());

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getPasswords()).hasSize(2).containsExactly("existing", "pass");
        assertThat(config.getKeys().getPasswordFile()).isNull();
    }

    @Test
    public void createNewLockedKeyCreatesNewPasswordFile() throws IOException {

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyAddToFile.json");
        final Path blankName = Files.createTempDirectory(UUID.randomUUID().toString());

        final Config config = factory.create(inputStream, null, blankName.toAbsolutePath().toString());

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getPasswords()).isNull();

        assertThat(config.getKeys().getPasswordFile()).isNotNull();
        final List<String> passes = Files.readAllLines(config.getKeys().getPasswordFile());
        assertThat(passes).hasSize(1).containsExactly("pass");
    }

    @Test
    public void cantAppendToPasswordFileThrowsError() throws IOException {

        Files.createFile(Paths.get("newPasses.txt"));
        Paths.get("newPasses.txt").toFile().setWritable(false);

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyAddToFile.json");
        final Path blankName = Files.createTempDirectory(UUID.randomUUID().toString());

        final Throwable throwable = catchThrowable(
            () -> factory.create(inputStream, null, blankName.toAbsolutePath().toString())
        );

        assertThat(throwable).hasMessage("Could not store new passwords: newPasses.txt");
    }

    @Test
    public void createNewLockedKeyWithNoPasswordsSet() throws IOException {

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyNoPasswordsSet.json");
        final Path blankName = Files.createTempDirectory(UUID.randomUUID().toString());

        final Config config = factory.create(inputStream, null, blankName.toAbsolutePath().toString());

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getPasswords()).isNull();

        assertThat(config.getKeys().getPasswordFile()).isEqualTo(Paths.get("passwords.txt"));
        final List<String> passes = Files.readAllLines(config.getKeys().getPasswordFile());
        assertThat(passes).hasSize(1).containsExactly("pass");
        Files.deleteIfExists(config.getKeys().getPasswordFile());
    }

    @Test
    public void unlockedKeyDoesntTriggerPasswordFile() throws IOException {

        final InputStream tempSystemIn = new ByteArrayInputStream((lineSeparator() + lineSeparator()).getBytes());
        System.setIn(tempSystemIn);
        this.factory = new JaxbConfigFactory();

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyNoPasswordsSet.json");
        final Path blankName = Files.createTempDirectory(UUID.randomUUID().toString());

        final Config config = factory.create(inputStream, null, blankName.toAbsolutePath().toString());

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getPasswords()).isNull();
        assertThat(config.getKeys().getPasswordFile()).isNull();
    }

    @Test
    public void ifExistingKeysWereUnlockedThenAddEmptyPassword() throws IOException {

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyWithUnlockedPrevious.json");
        final Path blankName = Files.createTempDirectory(UUID.randomUUID().toString());

        final Config config = factory.create(inputStream, null, blankName.toAbsolutePath().toString());

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).hasSize(2);
        assertThat(config.getKeys().getPasswords()).isNull();


        assertThat(config.getKeys().getPasswordFile()).isEqualTo(Paths.get("passwords.txt"));
        final List<String> passes = Files.readAllLines(config.getKeys().getPasswordFile());
        assertThat(passes).hasSize(2).containsExactly("", "pass");
        Files.deleteIfExists(config.getKeys().getPasswordFile());
    }

    @Test
    public void noNewKeyDoesntTriggerPasswords() {

        final InputStream tempSystemIn = new ByteArrayInputStream((lineSeparator() + lineSeparator()).getBytes());
        System.setIn(tempSystemIn);
        this.factory = new JaxbConfigFactory();

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyNoPasswordsSet.json");

        final Config config = factory.create(inputStream, null);

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).isEmpty();
        assertThat(config.getKeys().getPasswords()).isNull();
        assertThat(config.getKeys().getPasswordFile()).isNull();
    }

    @Test
    public void nullKeysDoesntCreatePasswords() {
        final InputStream tempSystemIn = new ByteArrayInputStream((lineSeparator() + lineSeparator()).getBytes());
        System.setIn(tempSystemIn);
        this.factory = new JaxbConfigFactory();

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/nullKeys.json");

        final Config config = factory.create(inputStream, null);

        assertThat(config.getKeys()).isNull();
    }

}
