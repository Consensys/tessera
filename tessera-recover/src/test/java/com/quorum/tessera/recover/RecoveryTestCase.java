package com.quorum.tessera.recover;

import com.quorum.tessera.config.*;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;

import java.util.*;

public abstract class RecoveryTestCase {


    protected PartyInfo getPartyInfo() {

        final String url = "http://myurl/";

        final Set<Party> parties = new HashSet<>();
        parties.add(new Party("http://party1"));
        parties.add(new Party("http://party2"));
        parties.add(new Party("http://party3"));
        parties.add(new Party("http://party4"));

        return new PartyInfo(url, Collections.emptySet(),parties);
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
