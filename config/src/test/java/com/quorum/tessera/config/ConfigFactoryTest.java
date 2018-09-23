package com.quorum.tessera.config;

import com.quorum.tessera.config.keypairs.InlineKeypair;
import com.quorum.tessera.test.util.ElUtil;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ConfigFactoryTest {

    @Test
    public void createFromSample() throws Exception {

        ConfigFactory configFactory = ConfigFactory.create();

        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");

        Map<String, Object> params = new HashMap<>();
        params.put("unixSocketPath", unixSocketPath.toString());

        InputStream configInputStream = ElUtil.process(getClass()
                .getResourceAsStream("/sample.json"), params);

        Config config = configFactory.create(configInputStream, Collections.emptyList());

        assertThat(config).isNotNull();
        assertThat(config.isUseWhiteList()).isFalse();
        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");
        assertThat(config.getPeers()).hasSize(2);
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0)).isInstanceOf(InlineKeypair.class);

        final KeyDataConfig keyDataConfig = ((InlineKeypair)config.getKeys().getKeyData().get(0)).getPrivateKeyConfig();
        final PrivateKeyData privateKeyData = keyDataConfig.getPrivateKeyData();

        assertThat(keyDataConfig).isNotNull();

        assertThat(keyDataConfig.getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(privateKeyData).isNotNull();

        assertThat(privateKeyData.getSnonce()).isEqualTo("x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC");
        assertThat(privateKeyData.getAsalt()).isEqualTo("7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=");
        assertThat(privateKeyData.getSbox()).isEqualTo("d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc");

        assertThat(privateKeyData.getArgonOptions()).isNotNull();
        assertThat(privateKeyData.getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(privateKeyData.getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(privateKeyData.getArgonOptions().getParallelism()).isEqualTo(4);
        assertThat(privateKeyData.getArgonOptions().getMemory()).isEqualTo(1048576);
    }

    @Test
    public void createFromSampleJaxbException() {

        final ConfigFactory configFactory = ConfigFactory.create();
        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        final InputStream inputStream = new ByteArrayInputStream("BANG".getBytes());

        final Throwable throwable = catchThrowable(() -> configFactory.create(inputStream, null));
        assertThat(throwable).isInstanceOf(ConfigException.class);
    }

    @Test
    public void createFromKeyGenSample() throws Exception {

        final Path tempFolder = Files.createTempDirectory(UUID.randomUUID().toString()).toAbsolutePath();

        final InlineKeypair keypair = new InlineKeypair(
            "publickey",
            new KeyDataConfig(
                new PrivateKeyData("value", "nonce", "salt", "box", new ArgonOptions("i", 10, 1048576, 4), "pass"),
                PrivateKeyType.LOCKED
            )
        );

        final ConfigFactory configFactory = ConfigFactory.create();
        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        Path unixSocketPath = Files.createTempFile(tempFolder, UUID.randomUUID().toString(), ".ipc");

        Map<String, Object> params = new HashMap<>();
        params.put("unixSocketPath", unixSocketPath.toString());

        InputStream configInputStream = ElUtil.process(getClass()
                .getResourceAsStream("/sample-private-keygen.json"), params);

        Config config = configFactory.create(configInputStream, singletonList(keypair));

        assertThat(config).isNotNull();
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0)).isInstanceOf(InlineKeypair.class);

        KeyDataConfig keyDataConfig = ((InlineKeypair)config.getKeys().getKeyData().get(0)).getPrivateKeyConfig();

        assertThat(keyDataConfig.getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(keyDataConfig.getPrivateKeyData()).isNotNull();

        assertThat(keyDataConfig.getPrivateKeyData().getSnonce()).isNotEmpty();
        assertThat(keyDataConfig.getPrivateKeyData().getAsalt()).isNotEmpty();
        assertThat(keyDataConfig.getPrivateKeyData().getSbox()).isNotEmpty();

        assertThat(keyDataConfig.getPrivateKeyData().getArgonOptions()).isNotNull();
        assertThat(keyDataConfig.getPrivateKeyData().getArgonOptions().getAlgorithm()).isEqualTo("i");
        assertThat(keyDataConfig.getPrivateKeyData().getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(keyDataConfig.getPrivateKeyData().getArgonOptions().getParallelism()).isEqualTo(4);
        assertThat(keyDataConfig.getPrivateKeyData().getArgonOptions().getMemory()).isEqualTo(1048576);
    }
}
