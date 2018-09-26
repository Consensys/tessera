package com.quorum.tessera.config;

import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.InlineKeypair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class JaxbConfigFactoryTest {

    private JaxbConfigFactory factory;

    private ConfigKeyPair sampleGeneratedKey;

    @Before
    public void init() {
        this.sampleGeneratedKey = new InlineKeypair(
            "publickey",
            new KeyDataConfig(
                new PrivateKeyData("value", "nonce", "salt", "box", new ArgonOptions("i", 1, 1, 1), "pass"),
                PrivateKeyType.LOCKED
            )
        );

        this.factory = new JaxbConfigFactory();
    }

    @After
    public void after() throws IOException {
        Files.deleteIfExists(Paths.get("newPasses.txt"));
        Files.deleteIfExists(Paths.get("passwords.txt"));
    }

    @Test
    public void createNewLockedKeyAddPasswordToInline() {

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyAddInline.json");
        this.sampleGeneratedKey.withPassword("pass");

        final Config config = factory.create(inputStream, singletonList(sampleGeneratedKey));

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getPasswords()).hasSize(1).containsExactlyInAnyOrder("pass");
        assertThat(config.getKeys().getPasswordFile()).isNull();
    }

    @Test
    public void createNewLockedKeyAppendsToList() {

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyAddInlineWithExisting.json");
        this.sampleGeneratedKey.withPassword("pass");

        final Config config = factory.create(inputStream, singletonList(sampleGeneratedKey));

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getPasswords()).hasSize(2).containsExactly("existing", "pass");
        assertThat(config.getKeys().getPasswordFile()).isNull();
    }

    @Test
    public void createNewLockedKeyCreatesNewPasswordFile() throws IOException {

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyAddToFile.json");
        this.sampleGeneratedKey.withPassword("pass");

        final Config config = factory.create(inputStream, singletonList(sampleGeneratedKey));

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

        final Throwable throwable = catchThrowable(() -> factory.create(inputStream, singletonList(sampleGeneratedKey)));

        assertThat(throwable).hasMessage("Could not store new passwords: newPasses.txt");
    }

    @Test
    public void createNewLockedKeyWithNoPasswordsSet() throws IOException {

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyNoPasswordsSet.json");
        this.sampleGeneratedKey.withPassword("pass");

        final Config config = factory.create(inputStream, singletonList(sampleGeneratedKey));

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getPasswords()).isNull();

        assertThat(config.getKeys().getPasswordFile()).isEqualTo(Paths.get("passwords.txt"));
        final List<String> passes = Files.readAllLines(config.getKeys().getPasswordFile());
        assertThat(passes).hasSize(1).containsExactly("pass");
        Files.deleteIfExists(config.getKeys().getPasswordFile());
    }

    @Test
    public void unlockedKeyDoesntTriggerPasswordFile() {

        final ConfigKeyPair unlockedSampleGeneratedKey = new InlineKeypair(
            "publickey",
            new KeyDataConfig(new PrivateKeyData("value", null, null, null, null, null), PrivateKeyType.UNLOCKED)
        );

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyNoPasswordsSet.json");

        final Config config = factory.create(inputStream, singletonList(unlockedSampleGeneratedKey));

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getPasswords()).isNull();
        assertThat(config.getKeys().getPasswordFile()).isNull();
    }

    @Test
    public void ifExistingKeysWereUnlockedThenAddEmptyPassword() throws IOException {

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyWithUnlockedPrevious.json");
        this.sampleGeneratedKey.withPassword("pass");

        final Config config = factory.create(inputStream, singletonList(sampleGeneratedKey));

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

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/newLockedKeyNoPasswordsSet.json");

        final Config config = factory.create(inputStream, emptyList());

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).isEmpty();
        assertThat(config.getKeys().getPasswords()).isNull();
        assertThat(config.getKeys().getPasswordFile()).isNull();
    }

    @Test
    public void nullKeysDoesntCreatePasswords() {

        final InputStream inputStream = getClass().getResourceAsStream("/keypassupdate/nullKeys.json");

        final Config config = factory.create(inputStream, emptyList());

        assertThat(config.getKeys()).isNull();
    }

}
