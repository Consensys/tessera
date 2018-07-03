package com.github.nexus.config;

import com.github.nexus.test.util.ElUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ConfigFactoryTest {

    @Test
    public void createFromSample() throws Exception {

        ConfigFactory configFactory = ConfigFactory.create();

        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        InputStream inputStream = getClass().getResourceAsStream("/sample.json");

        Config config = configFactory.create(inputStream);
        assertThat(config).isNotNull();

        assertThat(config.isUseWhiteList()).isFalse();

        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");

        assertThat(config.getPeers()).hasSize(2);

        assertThat(config.getKeys()).hasSize(1);

        PrivateKey privateKey = config.getKeys().stream()
                .map(KeyData::getPrivateKey).findAny().get();

        assertThat(privateKey.getConfig().getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(privateKey.getConfig().getPrivateKeyData()).isNotNull();

        assertThat(privateKey.getConfig().getPrivateKeyData().getSnonce()).isEqualTo("x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC");
        assertThat(privateKey.getConfig().getPrivateKeyData().getAsalt()).isEqualTo("7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=");
        assertThat(privateKey.getConfig().getPrivateKeyData().getSbox()).isEqualTo("d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc");

        assertThat(privateKey.getConfig().getPrivateKeyData().getArgonOptions()).isNotNull();
        assertThat(privateKey.getConfig().getPrivateKeyData().getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(privateKey.getConfig().getPrivateKeyData().getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(privateKey.getConfig().getPrivateKeyData().getArgonOptions().getParallelism()).isEqualTo(4);
        assertThat(privateKey.getConfig().getPrivateKeyData().getArgonOptions().getMemory()).isEqualTo(1048576);

    }

    @Test(expected = ConfigException.class)
    public void createFromSampleJaxbException() throws Exception {

        ConfigFactory configFactory = ConfigFactory.create();

        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        InputStream inputStream = new ByteArrayInputStream("BANG".getBytes());

        configFactory.create(inputStream);

    }

    @Test
    public void createFromSampleKeyFilePathsOnly() throws Exception {

        ConfigFactory configFactory = ConfigFactory.create();

        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        Path keyFile = Paths.get(getClass().getResource("/lockedprivatekey.json").toURI());

        Map<String, Object> params = new HashMap<>();
        params.put("privateKeyFile", keyFile.toAbsolutePath().toString());

        InputStream inputStream = ElUtil.process(getClass().getResourceAsStream("/sample-private-key-file-path.json"), params);

        Config config = configFactory.create(inputStream);
        assertThat(config).isNotNull();

        assertThat(config.isUseWhiteList()).isFalse();

        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");

        assertThat(config.getPeers()).hasSize(2);

        assertThat(config.getKeys()).hasSize(1);

        PrivateKey privateKey = config.getKeys().stream()
                .map(KeyData::getPrivateKey)
                .findAny().get();

        PrivateKeyConfig privateKeyConfig = privateKey.getConfig();

        assertThat(privateKeyConfig.getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(privateKeyConfig.getPrivateKeyData()).isNotNull();

        assertThat(privateKeyConfig.getPrivateKeyData().getSnonce()).isEqualTo("x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC");
        assertThat(privateKeyConfig.getPrivateKeyData().getAsalt()).isEqualTo("7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=");
        assertThat(privateKeyConfig.getPrivateKeyData().getSbox()).isEqualTo("d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc");

        assertThat(privateKeyConfig.getPrivateKeyData().getArgonOptions()).isNotNull();
        assertThat(privateKeyConfig.getPrivateKeyData().getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(privateKeyConfig.getPrivateKeyData().getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(privateKeyConfig.getPrivateKeyData().getArgonOptions().getParallelism()).isEqualTo(4);
        assertThat(privateKeyConfig.getPrivateKeyData().getArgonOptions().getMemory()).isEqualTo(1048576);

    }

    @Test
    public void createFromKeyGenSample() throws Exception {

        ConfigFactory configFactory = ConfigFactory.create();

        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        Path keyFile = Paths.get(getClass().getResource("/lockedprivatekey.json").toURI());
        Map<String, Object> params = new HashMap<>();
        params.put("privateKeyFile", keyFile.toAbsolutePath().toString());

        Path configFile = Paths.get(getClass().getResource("/sample-private-keygen.json").toURI());

        InputStream inputStream = ElUtil.process(Files.newInputStream(configFile), params);

        Config config = configFactory.create(inputStream);
        assertThat(config).isNotNull();

        assertThat(config.isUseWhiteList()).isFalse();

        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");

        assertThat(config.getPeers()).hasSize(2);

        assertThat(config.getKeys()).hasSize(1);

        PrivateKeyConfig privateKey = config.getKeys().stream()
                .map(KeyData::getPrivateKey)
                .map(PrivateKey::getConfig)
                .findAny().get();

        assertThat(privateKey.getType()).isEqualTo(PrivateKeyType.LOCKED);

        assertThat(privateKey.getPrivateKeyData()).isNotNull();

        assertThat(privateKey.getPrivateKeyData().getSnonce()).isEqualTo("x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC");
        assertThat(privateKey.getPrivateKeyData().getAsalt()).isEqualTo("7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=");
        assertThat(privateKey.getPrivateKeyData().getSbox()).isEqualTo("d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc");

        assertThat(privateKey.getPrivateKeyData().getArgonOptions()).isNotNull();
        assertThat(privateKey.getPrivateKeyData().getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(privateKey.getPrivateKeyData().getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(privateKey.getPrivateKeyData().getArgonOptions().getParallelism()).isEqualTo(4);
        assertThat(privateKey.getPrivateKeyData().getArgonOptions().getMemory()).isEqualTo(1048576);

    }
}
