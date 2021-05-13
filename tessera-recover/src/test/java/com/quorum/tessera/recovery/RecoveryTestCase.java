package com.quorum.tessera.recovery;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.*;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.version.BaseVersion;
import com.quorum.tessera.version.EnhancedPrivacyVersion;
import java.util.*;

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

  protected Set<NodeInfo> getAllNodeInfos() {

    final NodeInfo node1 =
        NodeInfo.Builder.create()
            .withUrl(NodeUri.create("http://party1").asString())
            .withRecipients(Set.of(mock(Recipient.class)))
            .withSupportedApiVersions(
                Set.of(BaseVersion.API_VERSION_1, EnhancedPrivacyVersion.API_VERSION_2))
            .build();

    final NodeInfo node2 =
        NodeInfo.Builder.create()
            .withUrl(NodeUri.create("http://party2").asString())
            .withRecipients(Set.of(mock(Recipient.class)))
            .withSupportedApiVersions(Set.of(BaseVersion.API_VERSION_1))
            .build();

    final NodeInfo node3 =
        NodeInfo.Builder.create()
            .withUrl(NodeUri.create("http://party3").asString())
            .withRecipients(Set.of(mock(Recipient.class)))
            .withSupportedApiVersions(
                Set.of(BaseVersion.API_VERSION_1, EnhancedPrivacyVersion.API_VERSION_2))
            .build();

    final NodeInfo node4 =
        NodeInfo.Builder.create()
            .withUrl(NodeUri.create("http://party4").asString())
            .withRecipients(Set.of(mock(Recipient.class)))
            .withSupportedApiVersions(Set.of(BaseVersion.API_VERSION_1))
            .build();

    //        final NodeInfo own = NodeInfo.Builder.create()
    //            .withUrl(NodeUri.create("http://myurl/").asString())
    //            .withRecipients(Set.of(mock(Recipient.class)))
    //            .withSupportedApiVersions(ApiVersion.versions())
    //            .build();

    return Set.of(node1, node2, node3, node4);
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
