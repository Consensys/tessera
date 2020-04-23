package com.quorum.tessera.recover;

import com.quorum.tessera.config.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RecoveryFactoryTest {

    @Test
    public void doStuff() {
        RecoveryFactory recoveryFactory = RecoveryFactory.newFactory();

        ConfigFactory.create();

        Config config = new Config();

        config.setEncryptor(new EncryptorConfig());
        config.getEncryptor().setType(EncryptorType.NACL);

        config.setKeys(new KeyConfiguration());
        config.getKeys().setKeyData(new ArrayList<>());

        KeyData keyData = new KeyData();
        keyData.setPublicKey("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        keyData.setPrivateKey("yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");
        config.getKeys().setKeyData(List.of(keyData));

        config.setJdbcConfig(new JdbcConfig());
        config.getJdbcConfig().setUrl("jdbc:h2:mem:test_mem");
        config.getJdbcConfig().setUsername("junit");
        config.getJdbcConfig().setPassword("");

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setServerAddress("http://localhost:8989");

        config.setServerConfigs(List.of(serverConfig));

        Recovery recovery = recoveryFactory.create(config);

        assertThat(recovery).isNotNull();


    }


}
