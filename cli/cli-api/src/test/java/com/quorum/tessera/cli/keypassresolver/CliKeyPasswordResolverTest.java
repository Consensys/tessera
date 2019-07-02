package com.quorum.tessera.cli.keypassresolver;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keypairs.InlineKeypair;
import com.quorum.tessera.passwords.PasswordReader;
import com.quorum.tessera.passwords.PasswordReaderFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CliKeyPasswordResolverTest {

    @Rule public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    private PasswordReader passwordReader;

    private CliKeyPasswordResolver cliKeyPasswordResolver;

    @Before
    public void init() {
        this.passwordReader = mock(PasswordReader.class);

        this.cliKeyPasswordResolver = new CliKeyPasswordResolver(passwordReader);
    }

    @Test
    public void defaultConstructorCreatesReaderInstanceFromFactory() throws ReflectiveOperationException {
        final CliKeyPasswordResolver resolver = new CliKeyPasswordResolver();

        final Field field = resolver.getClass().getDeclaredField("passwordReader");
        field.setAccessible(true);
        final Object obj = field.get(resolver);

        assertThat(obj).isInstanceOf(PasswordReaderFactory.create().getClass());
    }

    @Test
    public void emptyPasswordsReturnsSameKeys() {

        // null paths since we won't actually be reading them
        final ConfigKeyPair keypair = new FilesystemKeyPair(null, null);
        final KeyConfiguration keyConfig = new KeyConfiguration(null, emptyList(), singletonList(keypair), null, null);
        final Config config = new Config();
        config.setKeys(keyConfig);

        this.cliKeyPasswordResolver.resolveKeyPasswords(config);

        assertThat(keyConfig.getKeyData()).hasSize(1);
        final ConfigKeyPair returned = keyConfig.getKeyData().get(0);

        // passwords are always non-null, set to empty string if not present or not needed
        assertThat(returned.getPassword()).isNull();
        assertThat(returned).isSameAs(keypair);
    }

    @Test
    public void noPasswordsReturnsSameKeys() {

        // null paths since we won't actually be reading them
        final ConfigKeyPair keypair = new FilesystemKeyPair(null, null);
        final KeyConfiguration keyConfig = new KeyConfiguration(null, null, singletonList(keypair), null, null);
        final Config config = new Config();
        config.setKeys(keyConfig);

        this.cliKeyPasswordResolver.resolveKeyPasswords(config);

        assertThat(keyConfig.getKeyData()).hasSize(1);
        final ConfigKeyPair returned = keyConfig.getKeyData().get(0);

        // passwords are always non-null, set to empty string if not present or not needed
        assertThat(returned.getPassword()).isNull();
        assertThat(returned).isSameAs(keypair);
    }

    @Test
    public void passwordsAssignedToKeys() {

        // null paths since we won't actually be reading them
        final ConfigKeyPair keypair = new FilesystemKeyPair(null, null);
        final KeyConfiguration keyConfig =
                new KeyConfiguration(
                        null, singletonList("passwordsAssignedToKeys"), singletonList(keypair), null, null);
        final Config config = new Config();
        config.setKeys(keyConfig);

        this.cliKeyPasswordResolver.resolveKeyPasswords(config);

        assertThat(keyConfig.getKeyData()).hasSize(1);
        final ConfigKeyPair returned = keyConfig.getKeyData().get(0);
        assertThat(returned.getPassword()).isEqualTo("passwordsAssignedToKeys");
    }

    @Test
    public void unreadablePasswordFileGivesNoPasswords() throws IOException {

        final Path passes = Files.createTempDirectory("testdirectory").resolve("nonexistantfile.txt");

        final ConfigKeyPair keypair = new FilesystemKeyPair(null, null);
        final KeyConfiguration keyConfig = new KeyConfiguration(passes, null, singletonList(keypair), null, null);
        final Config config = new Config();
        config.setKeys(keyConfig);

        this.cliKeyPasswordResolver.resolveKeyPasswords(config);

        assertThat(keyConfig.getKeyData()).hasSize(1);
        final ConfigKeyPair returned = keyConfig.getKeyData().get(0);
        assertThat(returned.getPassword()).isNull();
    }

    @Test
    public void readablePasswordFileAssignsPasswords() throws IOException {

        final Path passes = Files.createTempDirectory("testdirectory").resolve("passwords.txt");
        Files.write(passes, "q".getBytes());

        final ConfigKeyPair keypair = new FilesystemKeyPair(null, null);
        final KeyConfiguration keyConfig = new KeyConfiguration(passes, null, singletonList(keypair), null, null);
        final Config config = new Config();
        config.setKeys(keyConfig);

        this.cliKeyPasswordResolver.resolveKeyPasswords(config);

        assertThat(keyConfig.getKeyData()).hasSize(1);
        final ConfigKeyPair returned = keyConfig.getKeyData().get(0);
        assertThat(returned.getPassword()).isEqualTo("q");
    }

    @Test
    public void nullKeyConfigReturns() {
        final Throwable throwable = catchThrowable(() -> this.cliKeyPasswordResolver.resolveKeyPasswords(new Config()));

        assertThat(throwable).isNull();
    }

    @Test
    public void gettingPasswordForNonInlineOrFileSystemKeyReturns() {

        final ConfigKeyPair keyPair = new DirectKeyPair("public", "private");

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        assertThat(keyPair.getPassword()).isNullOrEmpty();
    }

    @Test
    public void nullInlineKeyDoesntReadPassword() {

        final ConfigKeyPair keyPair = new FilesystemKeyPair(null, null);

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        assertThat(keyPair.getPassword()).isNullOrEmpty();
    }

    @Test
    public void unlockedKeyDoesntReadPassword() {
        final KeyDataConfig privKeyDataConfig =
                new KeyDataConfig(
                        new PrivateKeyData("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=", null, null, null, null),
                        PrivateKeyType.UNLOCKED);

        final InlineKeypair keyPair = new InlineKeypair("public", privKeyDataConfig);

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        assertThat(keyPair.getPassword()).isNullOrEmpty();
    }

    @Test
    public void lockedKeyWithEmptyPasswordRequestsPassword() {
        when(passwordReader.readPasswordFromConsole()).thenReturn("a");

        final KeyDataConfig privKeyDataConfig =
                new KeyDataConfig(
                        new PrivateKeyData(
                                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                                "yb7M8aRJzgxoJM2NecAPcmSVWDW1tRjv",
                                "MIqkFlgR2BWEpx2U0rObGg==",
                                "Gtvp1t6XZEiFVyaE/LHiP1+yvOIBBoiOL+bKeqcKgpiNt4j1oDDoqCC47UJpmQRC",
                                new ArgonOptions("i", 10, 1048576, 4)),
                        PrivateKeyType.LOCKED);

        final InlineKeypair keyPair = new InlineKeypair("public", privKeyDataConfig);
        keyPair.withPassword("");

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        assertThat(systemOutRule.getLog())
                .containsOnlyOnce(
                        "Password for key[0] missing or invalid.\nAttempt 1 of 2. Enter a password for the key");
    }

    @Test
    public void lockedKeyWithInvalidPasswordRequestsPassword() {
        when(passwordReader.readPasswordFromConsole()).thenReturn("a");

        final KeyDataConfig privKeyDataConfig =
                new KeyDataConfig(
                        new PrivateKeyData(
                                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                                "yb7M8aRJzgxoJM2NecAPcmSVWDW1tRjv",
                                "MIqkFlgR2BWEpx2U0rObGg==",
                                "Gtvp1t6XZEiFVyaE/LHiP1+yvOIBBoiOL+bKeqcKgpiNt4j1oDDoqCC47UJpmQRC",
                                new ArgonOptions("i", 10, 1048576, 4)),
                        PrivateKeyType.LOCKED);

        final InlineKeypair keyPair = new InlineKeypair("public", privKeyDataConfig);
        keyPair.withPassword("invalidPassword");

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        assertThat(systemOutRule.getLog())
                .containsOnlyOnce(
                        "Password for key[0] missing or invalid.\nAttempt 1 of 2. Enter a password for the key");
    }

    @Test
    public void invalidRequestedPasswordRerequests() {
        when(passwordReader.readPasswordFromConsole()).thenReturn("invalid", "a");

        final KeyDataConfig privKeyDataConfig =
                new KeyDataConfig(
                        new PrivateKeyData(
                                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                                "yb7M8aRJzgxoJM2NecAPcmSVWDW1tRjv",
                                "MIqkFlgR2BWEpx2U0rObGg==",
                                "Gtvp1t6XZEiFVyaE/LHiP1+yvOIBBoiOL+bKeqcKgpiNt4j1oDDoqCC47UJpmQRC",
                                new ArgonOptions("i", 10, 1048576, 4)),
                        PrivateKeyType.LOCKED);

        final InlineKeypair keyPair = new InlineKeypair("public", privKeyDataConfig);
        keyPair.withPassword("invalidPassword");

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        // work around for checking string appears twice in message
        assertThat(systemOutRule.getLog())
                .containsOnlyOnce(
                        "Password for key[0] missing or invalid.\nAttempt 1 of 2. Enter a password for the key")
                .containsOnlyOnce(
                        "Password for key[0] missing or invalid.\nAttempt 2 of 2. Enter a password for the key");
    }
}
