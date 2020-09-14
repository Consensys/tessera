package com.quorum.tessera.recovery;

import com.quorum.tessera.config.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;

import java.util.*;

import static org.mockito.Mockito.mock;

public abstract class RecoveryTestCase {

    protected NodeInfo getCurrent() {

        final String url = "http://myurl/";

        final Set<Recipient> recipients = new HashSet<>();
        recipients.add(Recipient.of(mock(PublicKey.class), "http://party1"));
        recipients.add(Recipient.of(mock(PublicKey.class), "http://party2"));
        recipients.add(Recipient.of(mock(PublicKey.class), "http://party3"));
        recipients.add(Recipient.of(mock(PublicKey.class), "http://party4"));
        recipients.add(Recipient.of(mock(PublicKey.class), "http://myurl/"));

        return NodeInfo.Builder.create().withUrl(url).withRecipients(recipients).build();
    }

    protected Config getConfig() {
        final Config config = new Config();

        config.setEncryptor(new EncryptorConfig());
        config.getEncryptor().setType(EncryptorType.NACL);

        config.setKeys(new KeyConfiguration());
        config.getKeys().setKeyData(new ArrayList<>());

        final KeyData keyData = new KeyData();
        keyData.setPublicKey("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        keyData.setPrivateKey("yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");
        config.getKeys().setKeyData(List.of(keyData));

        config.setJdbcConfig(new JdbcConfig());
        config.getJdbcConfig().setUrl("jdbc:h2:mem:test_mem");
        config.getJdbcConfig().setUsername("junit");
        config.getJdbcConfig().setPassword("");

        final ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setServerAddress("http://localhost:8989/");

        config.setServerConfigs(List.of(serverConfig));

        return config;
    }
}
