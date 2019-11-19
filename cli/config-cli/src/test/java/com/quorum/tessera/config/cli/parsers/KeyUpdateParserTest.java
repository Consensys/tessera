package com.quorum.tessera.config.cli.parsers;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.passwords.PasswordReader;
import org.apache.commons.cli.*;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.UnmarshalException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class KeyUpdateParserTest {

    private PasswordReader passwordReader;

    private KeyUpdateParser parser;

    private Options options;

    private KeyEncryptor keyEncryptor;

    @Before
    public void init() {
        this.passwordReader = mock(PasswordReader.class);
        when(passwordReader.requestUserPassword()).thenReturn("newPassword");

        this.keyEncryptor = mock(KeyEncryptor.class);
        this.parser = new KeyUpdateParser(keyEncryptor, passwordReader);

        this.options = new Options();

        this.options.addOption(Option.builder().longOpt("keys.keyData.config.data.aopts.algorithm").hasArg().build());
        this.options.addOption(Option.builder().longOpt("keys.keyData.config.data.aopts.iterations").hasArg().build());
        this.options.addOption(Option.builder().longOpt("keys.keyData.config.data.aopts.memory").hasArg().build());
        this.options.addOption(Option.builder().longOpt("keys.keyData.config.data.aopts.parallelism").hasArg().build());

        this.options.addOption(Option.builder().longOpt("keys.passwords").hasArg().build());
        this.options.addOption(Option.builder().longOpt("keys.passwordFile").hasArg().build());

        this.options.addOption(Option.builder().longOpt("keys.keyData.privateKeyPath").hasArg().build());
    }

    // Argon Option tests
    @Test
    public void noArgonOptionsGivenHasDefaults() throws ParseException {
        final CommandLine commandLine = new DefaultParser().parse(options, new String[] {});

        final ArgonOptions argonOptions = KeyUpdateParser.argonOptions(commandLine);

        assertThat(argonOptions.getAlgorithm()).isEqualTo("i");
        assertThat(argonOptions.getParallelism()).isEqualTo(4);
        assertThat(argonOptions.getMemory()).isEqualTo(1048576);
        assertThat(argonOptions.getIterations()).isEqualTo(10);
    }

    @Test
    public void argonOptionsGivenHasOverrides() throws ParseException {
        final String[] args =
                new String[] {
                    "--keys.keyData.config.data.aopts.algorithm", "d",
                    "--keys.keyData.config.data.aopts.memory", "100",
                    "--keys.keyData.config.data.aopts.iterations", "100",
                    "--keys.keyData.config.data.aopts.parallelism", "100"
                };
        final CommandLine commandLine = new DefaultParser().parse(options, args);

        final ArgonOptions argonOptions = KeyUpdateParser.argonOptions(commandLine);

        assertThat(argonOptions.getAlgorithm()).isEqualTo("d");
        assertThat(argonOptions.getParallelism()).isEqualTo(100);
        assertThat(argonOptions.getMemory()).isEqualTo(100);
        assertThat(argonOptions.getIterations()).isEqualTo(100);
    }

    // Password reading tests
    @Test
    public void inlinePasswordParsed() throws ParseException, IOException {
        final String[] args = new String[] {"--keys.passwords", "pass"};
        final CommandLine commandLine = new DefaultParser().parse(options, args);

        final List<String> passwords = KeyUpdateParser.passwords(commandLine);

        assertThat(passwords).isNotNull().hasSize(1).containsExactly("pass");
    }

    @Test
    public void passwordFileParsedAndRead() throws ParseException, IOException {
        final Path passwordFile = Files.createTempFile("passwords", ".txt");
        Files.write(passwordFile, "passwordInsideFile\nsecondPassword".getBytes());

        final String[] args = new String[] {"--keys.passwordFile", passwordFile.toAbsolutePath().toString()};
        final CommandLine commandLine = new DefaultParser().parse(options, args);

        final List<String> passwords = KeyUpdateParser.passwords(commandLine);

        assertThat(passwords).isNotNull().hasSize(2).containsExactly("passwordInsideFile", "secondPassword");
    }

    @Test
    public void passwordFileThrowsErrorIfCantBeRead() throws ParseException {
        final String[] args = new String[] {"--keys.passwordFile", "/tmp/passwords.txt"};
        final CommandLine commandLine = new DefaultParser().parse(options, args);

        final Throwable throwable = catchThrowable(() -> KeyUpdateParser.passwords(commandLine));

        assertThat(throwable).isNotNull().isInstanceOf(IOException.class);
    }

    @Test
    public void emptyListGivenForNoPasswords() throws ParseException, IOException {
        final String[] args = new String[] {};
        final CommandLine commandLine = new DefaultParser().parse(options, args);

        final List<String> passwords = KeyUpdateParser.passwords(commandLine);

        assertThat(passwords).isNotNull().isEmpty();
    }

    // key file tests
    @Test
    public void noPrivateKeyGivenThrowsError() throws ParseException {
        final String[] args = new String[] {};
        final CommandLine commandLine = new DefaultParser().parse(options, args);

        final Throwable throwable = catchThrowable(() -> KeyUpdateParser.privateKeyPath(commandLine));

        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Private key path cannot be null when updating key password");
    }

    @Test
    public void cantReadPrivateKeyThrowsError() throws ParseException {
        final String[] args = new String[] {"--keys.keyData.privateKeyPath", "/tmp/nonexisting.txt"};
        final CommandLine commandLine = new DefaultParser().parse(options, args);

        final Throwable throwable = catchThrowable(() -> KeyUpdateParser.privateKeyPath(commandLine));

        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void privateKeyExistsReturnsPath() throws ParseException, IOException {
        final Path key = Files.createTempFile("key", ".key");

        final String[] args = new String[] {"--keys.keyData.privateKeyPath", key.toString()};
        final CommandLine commandLine = new DefaultParser().parse(options, args);

        final Path path = KeyUpdateParser.privateKeyPath(commandLine);

        assertThat(path).isEqualTo(key);
    }

    // key fetching tests
    @Test
    public void unlockedKeyReturnedProperly() {
        final KeyDataConfig kdc =
                new KeyDataConfig(
                        new PrivateKeyData("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", null, null, null, null),
                        PrivateKeyType.UNLOCKED);

        final PrivateKey key = this.parser.getExistingKey(kdc, emptyList());

        String encodedKeyValue = Base64.getEncoder().encodeToString(key.getKeyBytes());

        assertThat(encodedKeyValue).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    }

    @Test
    public void lockedKeyFailsWithNoPasswordsMatching() {

        final KeyDataConfig kdc =
                new KeyDataConfig(
                        new PrivateKeyData(
                                null,
                                "dwixVoY+pOI2FMuu4k0jLqN/naQiTzWe",
                                "JoPVq9G6NdOb+Ugv+HnUeA==",
                                "6Jd/MXn29fk6jcrFYGPb75l7sDJae06I3Y1Op+bZSZqlYXsMpa/8lLE29H0sX3yw",
                                new ArgonOptions("id", 1, 1024, 1)),
                        PrivateKeyType.LOCKED);

        final Throwable throwable = catchThrowable(() -> this.parser.getExistingKey(kdc, singletonList("wrong")));

        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Locked key but no valid password given");
    }

    @Test
    public void lockedKeySucceedsWithPasswordsMatching() {
        PrivateKeyData privateKeyData =
                new PrivateKeyData(
                        null,
                        "dwixVoY+pOI2FMuu4k0jLqN/naQiTzWe",
                        "JoPVq9G6NdOb+Ugv+HnUeA==",
                        "6Jd/MXn29fk6jcrFYGPb75l7sDJae06I3Y1Op+bZSZqlYXsMpa/8lLE29H0sX3yw",
                        new ArgonOptions("id", 1, 1024, 1));

        final KeyDataConfig kdc =
                new KeyDataConfig(
                        new PrivateKeyData(
                                null,
                                "dwixVoY+pOI2FMuu4k0jLqN/naQiTzWe",
                                "JoPVq9G6NdOb+Ugv+HnUeA==",
                                "6Jd/MXn29fk6jcrFYGPb75l7sDJae06I3Y1Op+bZSZqlYXsMpa/8lLE29H0sX3yw",
                                new ArgonOptions("id", 1, 1024, 1)),
                        PrivateKeyType.LOCKED);

        PrivateKey privateKey = mock(PrivateKey.class);
        when(privateKey.getKeyBytes()).thenReturn("SUCCESS".getBytes());
        when(keyEncryptor.decryptPrivateKey(privateKeyData, "testpassword")).thenReturn(privateKey);

        final PrivateKey result = this.parser.getExistingKey(kdc, singletonList("testpassword"));

        assertThat(result.getKeyBytes()).isEqualTo("SUCCESS".getBytes());
    }

    @Test
    public void lockedKeySucceedsWithAtleastOnePasswordsMatching() {

        PrivateKeyData privateKeyData = mock(PrivateKeyData.class);

        final KeyDataConfig kdc = new KeyDataConfig(privateKeyData, PrivateKeyType.LOCKED);

        PrivateKey privateKey = mock(PrivateKey.class);
        when(privateKey.getKeyBytes()).thenReturn("SUCCESS".getBytes());

        when(keyEncryptor.decryptPrivateKey(privateKeyData, "wrong")).thenReturn(null);
        when(keyEncryptor.decryptPrivateKey(privateKeyData, "testpassword")).thenReturn(privateKey);

        final PrivateKey key = this.parser.getExistingKey(kdc, Arrays.asList("wrong", "testpassword"));

        assertThat(key).isNotNull();

        assertThat(key.getKeyBytes()).isEqualTo("SUCCESS".getBytes());
    }

    @Test
    public void loadingMalformedKeyfileThrowsError() throws IOException, ParseException {
        final Path key = Files.createTempFile("key", ".key");
        Files.write(key, "BAD JSON DATA".getBytes());

        final String[] args = new String[] {"--keys.keyData.privateKeyPath", key.toString()};
        final CommandLine commandLine = new DefaultParser().parse(options, args);

        final Throwable throwable = catchThrowable(() -> this.parser.parse(commandLine));

        assertThat(throwable).isInstanceOf(ConfigException.class).hasCauseExactlyInstanceOf(UnmarshalException.class);
    }

    @Test
    public void keyGetsUpdated() throws IOException, ParseException {
        final KeyDataConfig startingKey =
                JaxbUtil.unmarshal(getClass().getResourceAsStream("/lockedprivatekey.json"), KeyDataConfig.class);

        final Path key = Files.createTempFile("key", ".key");
        Files.write(key, JaxbUtil.marshalToString(startingKey).getBytes());

        final String[] args =
                new String[] {
                    "--keys.keyData.privateKeyPath", key.toString(),
                    "--keys.passwords", "testpassword",
                    "--keys.keyData.config.data.aopts.algorithm", "id",
                    "--keys.keyData.config.data.aopts.memory", "2048",
                    "--keys.keyData.config.data.aopts.iterations", "1",
                    "--keys.keyData.config.data.aopts.parallelism", "1"
                };
        final CommandLine commandLine = new DefaultParser().parse(options, args);

        PrivateKey privatekey = mock(PrivateKey.class);
        when(keyEncryptor.decryptPrivateKey(any(PrivateKeyData.class), anyString())).thenReturn(privatekey);

        PrivateKeyData privateKeyData = mock(PrivateKeyData.class);

        when(keyEncryptor.encryptPrivateKey(any(PrivateKey.class), anyString(), any(ArgonOptions.class)))
                .thenReturn(privateKeyData);

        this.parser.parse(commandLine);

        final KeyDataConfig endingKey = JaxbUtil.unmarshal(Files.newInputStream(key), KeyDataConfig.class);

        assertThat(endingKey.getSbox()).isNotEqualTo(startingKey.getSbox());
        assertThat(endingKey.getSnonce()).isNotEqualTo(startingKey.getSnonce());
        assertThat(endingKey.getAsalt()).isNotEqualTo(startingKey.getAsalt());
    }

    @Test
    public void keyGetsUpdatedToNoPassword() throws IOException, ParseException {
        final KeyDataConfig startingKey =
                JaxbUtil.unmarshal(getClass().getResourceAsStream("/lockedprivatekey.json"), KeyDataConfig.class);

        when(passwordReader.requestUserPassword()).thenReturn("");

        final Path key = Files.createTempFile("key", ".key");
        Files.write(key, JaxbUtil.marshalToString(startingKey).getBytes());

        final String[] args =
                new String[] {
                    "--keys.keyData.privateKeyPath", key.toString(),
                    "--keys.passwords", "testpassword",
                    "--keys.keyData.config.data.aopts.algorithm", "id",
                    "--keys.keyData.config.data.aopts.memory", "2048",
                    "--keys.keyData.config.data.aopts.iterations", "1",
                    "--keys.keyData.config.data.aopts.parallelism", "1"
                };
        final CommandLine commandLine = new DefaultParser().parse(options, args);

        byte[] privateKeyData = "SOME PRIVATE DATA".getBytes();
        PrivateKey privateKey = PrivateKey.from(privateKeyData);
        when(keyEncryptor.decryptPrivateKey(any(PrivateKeyData.class), anyString())).thenReturn(privateKey);

        this.parser.parse(commandLine);

        final KeyDataConfig endingKey = JaxbUtil.unmarshal(Files.newInputStream(key), KeyDataConfig.class);

        assertThat(endingKey.getSbox()).isNotEqualTo(startingKey.getSbox());
        assertThat(endingKey.getSnonce()).isNotEqualTo(startingKey.getSnonce());
        assertThat(endingKey.getAsalt()).isNotEqualTo(startingKey.getAsalt());
        assertThat(endingKey.getPrivateKeyData().getValue())
                .isEqualTo(Base64.getEncoder().encodeToString(privateKeyData));
    }
}
