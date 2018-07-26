package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.SslTrustMode;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonObject;
import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class TomlConfigFactoryTest {

    private TomlConfigFactory tomlConfigFactory;

    @Before
    public void onSetup() throws Exception {
        tomlConfigFactory = new TomlConfigFactory();
    }

    @Test
    public void createConfigFromSampleFile() throws IOException {
        try (InputStream configData = getClass().getResourceAsStream("/sample.conf")) {
            Config result = tomlConfigFactory.create(configData);
            assertThat(result).isNotNull();
        }
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
    public void resolveSslTrustModeForCaOrTofu() {
        SslTrustMode result = TomlConfigFactory.resolve("ca-or-tofu");
        assertThat(result).isEqualTo(SslTrustMode.CA_OR_TOFU);

    }

    @Test
    public void resolveSslTrustMode() {

        java.util.Map<SslTrustMode, String> fixtures = new EnumMap<>(SslTrustMode.class);

        fixtures.put(SslTrustMode.CA, "ca");
        fixtures.put(SslTrustMode.TOFU, "tofu");
        fixtures.put(SslTrustMode.CA_OR_TOFU, "ca-or-tofu");
        fixtures.put(SslTrustMode.NONE, "none");

        for (SslTrustMode mode : fixtures.keySet()) {
            SslTrustMode result = TomlConfigFactory.resolve(fixtures.get(mode));
            assertThat(result).isEqualTo(mode);
        }
    }

    @Test
    public void resolveSslTrustModeNone() {

        assertThat(TomlConfigFactory.resolve(null)).isEqualTo(SslTrustMode.NONE);
        assertThat(TomlConfigFactory.resolve("BOGUS")).isEqualTo(SslTrustMode.NONE);
    }

    @Test
    public void createPrivateKeyData() throws Exception {

        JsonObject privateKeyData = Json.createObjectBuilder()
                .add("aopts",
                        Json.createObjectBuilder()
                                .add("variant", "id")
                                .add("memory", 1048576)
                                .add("iterations", 10)
                                .add("parallelism", 4)
                                .add("version", 1.3)
                )
                .add("snonce", "xx3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC")
                .add("asalt", "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=")
                .add("sbox", "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc").build();

        JsonObject keyDataConfigJson = Json.createObjectBuilder()
                .add("data", Json.createObjectBuilder(privateKeyData))
                .add("type", "argon2sbox").build();

        Path privateKeyPath = Files.createTempFile("createPrivateKeyData", ".txt");
        Files.write(privateKeyPath, keyDataConfigJson.toString().getBytes());

        List<KeyDataConfig> result = TomlConfigFactory.createPrivateKeyData(Arrays.asList(privateKeyPath.toString()), Arrays.asList("Secret"));

        assertThat(result).hasSize(1);

        KeyDataConfig keyConfig = result.get(0);

        assertThat(keyConfig.getType()).isEqualTo(PrivateKeyType.LOCKED);

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

}
