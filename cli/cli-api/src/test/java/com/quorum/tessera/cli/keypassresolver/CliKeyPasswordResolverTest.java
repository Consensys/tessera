package com.quorum.tessera.cli.keypassresolver;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keypairs.InlineKeypair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.passwords.PasswordReader;
import com.quorum.tessera.passwords.PasswordReaderFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import java.lang.reflect.Field;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

        final ConfigKeyPair keyPair = new FilesystemKeyPair(null, null, null);

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        assertThat(keyPair.getPassword()).isNullOrEmpty();
    }

    @Test
    public void unlockedKeyDoesntReadPassword() {
        final KeyDataConfig privKeyDataConfig =
                new KeyDataConfig(
                        new PrivateKeyData("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=", null, null, null, null),
                        PrivateKeyType.UNLOCKED);

        KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);

        final InlineKeypair keyPair = new InlineKeypair("public", privKeyDataConfig, keyEncryptor);

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        assertThat(keyPair.getPassword()).isNullOrEmpty();
    }

    @Test
    public void lockedKeyWithEmptyPasswordRequestsPassword() {
        when(passwordReader.readPasswordFromConsole()).thenReturn("a");

        final KeyDataConfig privKeyDataConfig = mock(KeyDataConfig.class);
        when(privKeyDataConfig.getType()).thenReturn(PrivateKeyType.LOCKED);
        PrivateKeyData privateKeyData = mock(PrivateKeyData.class);
        when(privKeyDataConfig.getPrivateKeyData()).thenReturn(privateKeyData);

        final InlineKeypair keyPair = mock(InlineKeypair.class);
        when(keyPair.getPassword()).thenReturn("");
        when(keyPair.getPrivateKeyConfig()).thenReturn(privKeyDataConfig);

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        assertThat(systemOutRule.getLog())
                .containsOnlyOnce(
                        "Password for key[0] missing or invalid.\nAttempt 1 of 2. Enter a password for the key");
    }

    @Test
    public void lockedKeyWithInvalidPasswordRequestsPassword() {
        when(passwordReader.readPasswordFromConsole()).thenReturn("a");

        final String validPassword = "a";
        final String invalidPassword = "invalidPassword";

        byte[] privateKeyBytes = Base64.getDecoder().decode("w+itzh2vfuGjiGYEVJtqpiJVUmI5vGUK4CzMErxa+GY=");
        final PrivateKey unlockedKey = PrivateKey.from(privateKeyBytes);

        final KeyDataConfig privKeyDataConfig =
            new KeyDataConfig(
                new PrivateKeyData(
                    "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                    "yb7M8aRJzgxoJM2NecAPcmSVWDW1tRjv",
                    "MIqkFlgR2BWEpx2U0rObGg==",
                    "Gtvp1t6XZEiFVyaE/LHiP1+yvOIBBoiOL+bKeqcKgpiNt4j1oDDoqCC47UJpmQRC",
                    new ArgonOptions("i", 10, 1048576, 4)),
                PrivateKeyType.LOCKED);

        KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
        when(keyEncryptor.decryptPrivateKey(any(PrivateKeyData.class), eq(invalidPassword))).thenThrow(new EncryptorException("decrypt failed"));
        when(keyEncryptor.decryptPrivateKey(any(PrivateKeyData.class), eq(validPassword))).thenReturn(unlockedKey);

        final InlineKeypair keyPair = new InlineKeypair("public", privKeyDataConfig, keyEncryptor);
        keyPair.withPassword(invalidPassword);

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        assertThat(systemOutRule.getLog())
            .containsOnlyOnce(
                "Password for key[0] missing or invalid.\nAttempt 1 of 2. Enter a password for the key");
    }

    @Test
    public void lockedKeyWithValidPasswordDoesNotRequestPassword() {
        final String validPassword = "a";

        byte[] privateKeyBytes = Base64.getDecoder().decode("w+itzh2vfuGjiGYEVJtqpiJVUmI5vGUK4CzMErxa+GY=");
        final PrivateKey unlockedKey = PrivateKey.from(privateKeyBytes);

        final KeyDataConfig privKeyDataConfig =
            new KeyDataConfig(
                new PrivateKeyData(
                    "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                    "yb7M8aRJzgxoJM2NecAPcmSVWDW1tRjv",
                    "MIqkFlgR2BWEpx2U0rObGg==",
                    "Gtvp1t6XZEiFVyaE/LHiP1+yvOIBBoiOL+bKeqcKgpiNt4j1oDDoqCC47UJpmQRC",
                    new ArgonOptions("i", 10, 1048576, 4)),
                PrivateKeyType.LOCKED);

        KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
        when(keyEncryptor.decryptPrivateKey(any(PrivateKeyData.class), eq(validPassword))).thenReturn(unlockedKey);

        final InlineKeypair keyPair = new InlineKeypair("public", privKeyDataConfig, keyEncryptor);
        keyPair.withPassword(validPassword);

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        verifyZeroInteractions(passwordReader);
    }

    @Test
    public void invalidRequestedPasswordRerequests() {

        when(passwordReader.readPasswordFromConsole()).thenReturn("invalid", "a");

        PrivateKeyData privateKeyData = mock(PrivateKeyData.class);

        final KeyDataConfig privKeyDataConfig = mock(KeyDataConfig.class);
        when(privKeyDataConfig.getPrivateKeyData()).thenReturn(privateKeyData);
        when(privKeyDataConfig.getType()).thenReturn(PrivateKeyType.LOCKED);

        final InlineKeypair keyPair = mock(InlineKeypair.class);
        when(keyPair.getPrivateKeyConfig()).thenReturn(privKeyDataConfig);

        keyPair.withPassword("invalidPassword");

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        // work around for checking string appears twice in message
        assertThat(systemOutRule.getLog())
                .containsOnlyOnce(
                        "Password for key[0] missing or invalid.\nAttempt 1 of 2. Enter a password for the key")
                .containsOnlyOnce(
                        "Password for key[0] missing or invalid.\nAttempt 2 of 2. Enter a password for the key");
    }

    @Test
    public void lockedKeyWithEncrptionErrorP() {
        when(passwordReader.readPasswordFromConsole()).thenReturn("a");

        final KeyDataConfig privKeyDataConfig = mock(KeyDataConfig.class);
        when(privKeyDataConfig.getType()).thenReturn(PrivateKeyType.LOCKED);
        PrivateKeyData privateKeyData = mock(PrivateKeyData.class);
        when(privKeyDataConfig.getPrivateKeyData()).thenReturn(privateKeyData);

        final InlineKeypair keyPair = mock(InlineKeypair.class);
        when(keyPair.getPrivateKeyConfig()).thenReturn(privKeyDataConfig);
        when(keyPair.getPrivateKey()).thenReturn("NACL_FAILURE");

        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);

        assertThat(systemOutRule.getLog())
                .containsOnlyOnce(
                        "Password for key[0] missing or invalid.\nAttempt 1 of 2. Enter a password for the key");
    }
}
