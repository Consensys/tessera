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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class TomlConfigFactoryTest {

    private TomlConfigFactory tomlConfigFactory;

    @Before
    public void onSetup() throws Exception {
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
            Config result = tomlConfigFactory.create(configData);
            assertThat(result).isNotNull();
            assertThat(result.getUnixSocketFile()).isEqualTo(Paths.get("data", "myipcfile.ipc"));
            assertThat(result.getServerConfig()).isNotNull();
            assertThat(result.getServerConfig().getSslConfig()).isNotNull();

            SslConfig sslConfig = result.getServerConfig().getSslConfig();

            assertThat(sslConfig.getClientTlsKeyPath()).isEqualTo(Paths.get("data/tls-client-key.pem"));
            assertThat(sslConfig.getClientTrustMode()).isEqualTo(SslTrustMode.CA_OR_TOFU);

        }

        Files.deleteIfExists(passwordFile);
    }

    @Test
    public void createConfigFromSampleFileOnly() throws IOException {

        Path passwordFile = Files.createTempFile("password", ".txt");
        InputStream template = getClass().getResourceAsStream("/sample.conf");



        try (InputStream configData = template) {
            Config result = tomlConfigFactory.create(configData);
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
                Config result = tomlConfigFactory.create(ammendedInput);
                assertThat(result).isNotNull();

            }
        }

        Files.deleteIfExists(passwordsFile);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createWithKeysNotSupported() throws IOException {
        InputStream configData = mock(InputStream.class);
        InputStream keyConfigData = mock(InputStream.class);

        tomlConfigFactory.create(configData, keyConfigData);
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

            Config result = tomlConfigFactory.create(configData);
            assertThat(result).isNotNull();

        }

    }
}
