package com.quorum.tessera.config.migration;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.migration.test.FixtureUtil;
import com.quorum.tessera.test.util.ElUtil;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

public class TomlConfigFactoryTest {

    private TomlConfigFactory tomlConfigFactory;

    @Before
    public void onSetup() {
        tomlConfigFactory = new TomlConfigFactory();
    }

    @Test
    public void createConfigFromSampleFile() throws IOException {

        Path passwordFile = Files.createTempFile("password", ".txt");
        InputStream template = getClass().getResourceAsStream("/sample-all-values.conf");

        Map<String, Object> params = new HashMap<String, Object>() {
            {
                put("passwordFile", passwordFile);
                put("serverKeyStorePath", "serverKeyStorePath");
            }
        };

        try (InputStream configData = ElUtil.process(template, params)) {
            Config result = tomlConfigFactory.create(configData, null).build();
            assertThat(result).isNotNull();
            assertThat(result.getUnixSocketFile()).isEqualTo(Paths.get("data", "myipcfile.ipc"));
            assertThat(result.getServerConfig()).isNotNull();
            assertThat(result.getServerConfig().getSslConfig()).isNotNull();

            SslConfig sslConfig = result.getServerConfig().getSslConfig();

            assertThat(result.getServerConfig().getHostName()).isEqualTo("http://127.0.0.1");
            assertThat(result.getServerConfig().getPort()).isEqualTo(9001);
            assertThat(result.getServerConfig().getBindingAddress()).isEqualTo("http://127.0.0.1:9001");

            assertThat(sslConfig.getClientTlsKeyPath()).isEqualTo(Paths.get("data/tls-client-key.pem"));
            assertThat(sslConfig.getClientTrustMode()).isEqualTo(SslTrustMode.CA_OR_TOFU);

        }

        Files.deleteIfExists(passwordFile);
    }

    @Test
    public void urlPortNotSetInConfig() {

        InputStream template = getClass().getResourceAsStream("/sample-all-values-urlport-not-present.conf");

        Config result = tomlConfigFactory.create(template, null).build();

        assertThat(result.getServerConfig().getHostName()).isEqualTo("http://127.0.0.1");


    }

    @Test
    public void badUrlSetInConfig() throws IOException {

        try (InputStream template = getClass().getResourceAsStream("/sample-all-values-bad-url.conf")) {
            tomlConfigFactory.create(template, null);
        } catch (RuntimeException ex) {
            assertThat(ex).hasMessage("Bad server url given: unknown protocol: ht");
        }

    }

    @Test
    public void createConfigFromSampleFileOnly() throws IOException {

        Path passwordFile = Files.createTempFile("password", ".txt");
        InputStream template = getClass().getResourceAsStream("/sample.conf");


        try (InputStream configData = template) {
            Config result = tomlConfigFactory.create(configData, null).build();
            assertThat(result).isNotNull();
            assertThat(result.getUnixSocketFile()).isEqualTo(Paths.get("data", "constellation.ipc"));
            assertThat(result.getServerConfig()).isNotNull();
            assertThat(result.getServerConfig().getSslConfig()).isNotNull();

            SslConfig sslConfig = result.getServerConfig().getSslConfig();

            assertThat(sslConfig.getClientTlsKeyPath()).isEqualTo(Paths.get("data/tls-client-key.pem"));
            assertThat(sslConfig.getClientTrustMode()).isEqualTo(SslTrustMode.CA_OR_TOFU);

        }

        Files.deleteIfExists(passwordFile);
    }


    @Test
    public void createConfigFromSampleFileAndAddedPasswordsFile() throws IOException {

        Path passwordsFile = Files.createTempFile("createConfigFromSampleFileAndAddedPasswordsFile", ".txt");

        List<String> passwordsFileLines = Arrays.asList("PASSWORD_1", "PASSWORD_2", "PASSWORD_3");

        Files.write(passwordsFile, passwordsFileLines);

        try (InputStream configData = getClass().getResourceAsStream("/sample.conf")) {

            List<String> lines = Stream.of(configData)
                .map(InputStreamReader::new)
                .map(BufferedReader::new)
                .flatMap(BufferedReader::lines)
                .collect(Collectors.toList());

            lines.add(String.format("passwords = \"%s\"", passwordsFile.toString()));

            final byte[] data = String.join(System.lineSeparator(), lines).getBytes();
            try (InputStream ammendedInput = new ByteArrayInputStream(data)) {
                Config result = tomlConfigFactory.create(ammendedInput, null).build();
                assertThat(result).isNotNull();

            }
        }

        Files.deleteIfExists(passwordsFile);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createWithKeysNotSupported() {
        InputStream configData = mock(InputStream.class);

        tomlConfigFactory.create(configData, null, "testKey");
    }

    @Test
    public void createPrivateKeyData() throws Exception {

        JsonObject keyDataConfigJson = FixtureUtil.createLockedPrivateKey();

        Path privateKeyPath = Files.createTempFile("createPrivateKeyData", ".txt");
        Files.write(privateKeyPath, keyDataConfigJson.toString().getBytes());

        List<KeyDataConfig> result = TomlConfigFactory
            .createPrivateKeyData(Arrays.asList(privateKeyPath.toString()), Arrays.asList("Secret"));

        assertThat(result).hasSize(1);

        KeyDataConfig keyConfig = result.get(0);

        assertThat(keyConfig.getType()).isEqualTo(PrivateKeyType.LOCKED);

        JsonObject privateKeyData = keyDataConfigJson.getJsonObject("data");

        PrivateKeyData key = keyConfig.getPrivateKeyData();

        assertThat(key.getPassword()).isEqualTo("Secret");
        assertThat(key.getAsalt()).isEqualTo(privateKeyData.getString("asalt"));
        assertThat(key.getSbox()).isEqualTo(privateKeyData.getString("sbox"));
        assertThat(key.getSnonce()).isEqualTo(privateKeyData.getString("snonce"));

        assertThat(key.getArgonOptions()).isNotNull();

        JsonObject argonOptions = privateKeyData.getJsonObject("aopts");

        assertThat(key.getArgonOptions().getIterations()).isEqualTo(argonOptions.getInt("iterations"));
        assertThat(key.getArgonOptions().getMemory()).isEqualTo(argonOptions.getInt("memory"));
        assertThat(key.getArgonOptions().getParallelism()).isEqualTo(argonOptions.getInt("parallelism"));
        assertThat(key.getArgonOptions().getAlgorithm()).isEqualTo(argonOptions.getString("variant"));

        Files.deleteIfExists(privateKeyPath);

    }

    @Test
    public void createUnlockedPrivateKeyData() throws Exception {

        JsonObject keyDataConfigJson = FixtureUtil.createUnlockedPrivateKey();

        Path privateKeyPath = Files.createTempFile("createUnlockedPrivateKeyData", ".txt");
        Files.write(privateKeyPath, keyDataConfigJson.toString().getBytes());

        List<KeyDataConfig> result = TomlConfigFactory
            .createPrivateKeyData(Arrays.asList(privateKeyPath.toString()), Arrays.asList("Secret"));

        assertThat(result).hasSize(1);

        KeyDataConfig keyConfig = result.get(0);

        assertThat(keyConfig.getType()).isEqualTo(PrivateKeyType.UNLOCKED);

        Files.deleteIfExists(privateKeyPath);

    }

    @Test
    public void createConfigFromNoPasswordsFile() throws IOException {

        try (InputStream configData = getClass().getResourceAsStream("/sample.conf")) {

            Config result = tomlConfigFactory.create(configData, null).build();
            assertThat(result).isNotNull();

        }

    }

    @Test
    public void ifPublicAndPrivateKeyListAreEmptyThenKeyConfigurationIsAllNulls() throws IOException {
        try (InputStream configData = getClass().getResourceAsStream("/sample-no-keys.conf")) {

            KeyConfiguration result = tomlConfigFactory.createKeyDataBuilder(configData).build();
            assertThat(result).isNotNull();

            KeyConfiguration expected = new KeyConfiguration(null, null, Collections.emptyList());
            assertThat(result).isEqualTo(expected);

        }
    }

    @Test
    public void ifPublicKeyListIsEmptyThenKeyConfigurationIsAllNulls() throws IOException {
        try (InputStream configData = getClass().getResourceAsStream("/sample-with-only-private-keys.conf")) {

            final Throwable throwable = catchThrowable(() -> tomlConfigFactory.createKeyDataBuilder(configData).build());

            assertThat(throwable)
                .isInstanceOf(ConfigException.class)
                .hasCauseExactlyInstanceOf(RuntimeException.class);

            assertThat(throwable.getCause()).hasMessage("Different amount of public and private keys supplied");

        }
    }

    @Test
    public void ifPrivateKeyListIsEmptyThenKeyConfigurationIsAllNulls() throws IOException {
        try (InputStream configData = getClass().getResourceAsStream("/sample-with-only-public-keys.conf")) {

            final Throwable throwable = catchThrowable(() -> tomlConfigFactory.createKeyDataBuilder(configData).build());

            assertThat(throwable)
                .isInstanceOf(ConfigException.class)
                .hasCauseExactlyInstanceOf(RuntimeException.class);

            assertThat(throwable.getCause()).hasMessage("Different amount of public and private keys supplied");

        }
    }
}
