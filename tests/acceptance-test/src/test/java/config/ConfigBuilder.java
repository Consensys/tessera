package config;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.DBType;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import suite.EnclaveType;
import suite.ExecutionContext;
import suite.SocketType;

public class ConfigBuilder {

  private final SslConfig sslConfig =
      new SslConfig(
          SslAuthenticationMode.STRICT,
          false,
          Paths.get(
              getClass().getResource("/certificates/server-localhost-with-san.jks").getFile()),
          "testtest".toCharArray(),
          Paths.get(getClass().getResource("/certificates/truststore.jks").getFile()),
          "testtest".toCharArray(),
          SslTrustMode.CA,
          Paths.get(getClass().getResource("/certificates/client.jks").getFile()).toAbsolutePath(),
          "testtest".toCharArray(),
          Paths.get(getClass().getResource("/certificates/truststore.jks").getFile()),
          "testtest".toCharArray(),
          SslTrustMode.CA,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null);

  private EncryptorConfig encryptorConfig;

  private Integer q2tPort;

  private Integer p2pPort;

  private Integer partyInfoInterval;

  private Integer thirdPartyPort;

  private Integer enclavePort;

  private ExecutionContext executionContext;

  private String nodeId;

  private Integer nodeNumber;

  private List<String> peerUrls = new ArrayList<>();

  private Map<String, String> keys = new HashMap<>();

  private List<String> alwaysSendTo = new ArrayList<>();

  private SocketType q2tSocketType;

  private FeatureToggles featureToggles;

  public ConfigBuilder withQ2TSocketType(SocketType q2tSocketType) {
    this.q2tSocketType = q2tSocketType;
    return this;
  }

  public ConfigBuilder withAlwaysSendTo(String alwaysSendTo) {
    this.alwaysSendTo.add(alwaysSendTo);
    return this;
  }

  public ConfigBuilder withKeys(Map<String, String> keys) {
    this.keys.putAll(keys);
    return this;
  }

  public ConfigBuilder withKeys(String publicKey, String privateKey) {
    this.keys.put(publicKey, privateKey);
    return this;
  }

  public ConfigBuilder withThirdPartyPort(Integer thirdPartyPort) {
    this.thirdPartyPort = thirdPartyPort;
    return this;
  }

  public ConfigBuilder withQt2Port(Integer q2tPort) {
    this.q2tPort = q2tPort;
    return this;
  }

  public ConfigBuilder withP2pPort(Integer p2pPort) {
    this.p2pPort = p2pPort;
    return this;
  }

  public ConfigBuilder withPartyInfoInterval(Integer partyInfoInterval) {
    this.partyInfoInterval = partyInfoInterval;
    return this;
  }

  public ConfigBuilder withEnclavePort(Integer enclavePort) {
    this.enclavePort = enclavePort;
    return this;
  }

  public ConfigBuilder withNodeNumber(Integer nodeNumber) {
    this.nodeNumber = nodeNumber;
    return this;
  }

  public ConfigBuilder withNodeId(String nodeId) {
    this.nodeId = nodeId;
    return this;
  }

  public ConfigBuilder withPeer(String peerUrl) {
    this.peerUrls.add(peerUrl);
    return this;
  }

  public ConfigBuilder withEncryptorConfig(EncryptorConfig encryptorConfig) {
    this.encryptorConfig = encryptorConfig;
    return this;
  }

  public ConfigBuilder withExecutionContext(ExecutionContext executionContext) {
    this.executionContext = executionContext;
    return this;
  }

  public ConfigBuilder withFeatureToggles(final FeatureToggles featureToggles) {
    this.featureToggles = featureToggles;
    return this;
  }

  public Config build() {

    Objects.requireNonNull(encryptorConfig, "no encryptorConfig defined");

    KeyEncryptorFactory.newFactory().create(encryptorConfig);

    final Config config = new Config();
    config.setEncryptor(encryptorConfig);
    JdbcConfig jdbcConfig = new JdbcConfig();

    jdbcConfig.setUrl(executionContext.getDbType().createUrl(nodeId, nodeNumber));
    jdbcConfig.setUsername("sa");
    jdbcConfig.setPassword("password");
    jdbcConfig.setAutoCreateTables(executionContext.isAutoCreateTables());
    config.setJdbcConfig(jdbcConfig);

    ServerConfig q2tServerConfig = new ServerConfig();
    q2tServerConfig.setApp(AppType.Q2T);
    q2tServerConfig.setCommunicationType(executionContext.getCommunicationType());

    if (executionContext.getCommunicationType() == CommunicationType.REST
        && (q2tSocketType == SocketType.UNIX
            || executionContext.getSocketType() == SocketType.UNIX)) {
      q2tServerConfig.setServerAddress(String.format("unix:/tmp/q2t-rest-unix-%d.ipc", nodeNumber));
    } else {
      q2tServerConfig.setServerAddress("http://localhost:" + q2tPort);
      q2tServerConfig.setBindingAddress("http://0.0.0.0:" + q2tPort);
    }

    List<ServerConfig> servers = new ArrayList<>();

    servers.add(q2tServerConfig);

    ServerConfig p2pServerConfig = new ServerConfig();
    p2pServerConfig.setApp(AppType.P2P);
    p2pServerConfig.setCommunicationType(executionContext.getCommunicationType());
    p2pServerConfig.setServerAddress("http://localhost:" + p2pPort);
    p2pServerConfig.setBindingAddress("http://0.0.0.0:" + p2pPort);
    if (Objects.nonNull(partyInfoInterval)) {
      p2pServerConfig.setProperties(
          Collections.singletonMap("partyInfoInterval", Integer.toString(partyInfoInterval)));
    }
    servers.add(p2pServerConfig);

    if (executionContext.getCommunicationType() == CommunicationType.REST
        && Objects.nonNull(thirdPartyPort)) {
      ServerConfig thirdPartyServerConfig = new ServerConfig();
      thirdPartyServerConfig.setApp(AppType.THIRD_PARTY);
      thirdPartyServerConfig.setServerAddress("http://localhost:" + thirdPartyPort);
      thirdPartyServerConfig.setBindingAddress("http://0.0.0.0:" + thirdPartyPort);
      thirdPartyServerConfig.setCommunicationType(CommunicationType.REST);

      servers.add(thirdPartyServerConfig);
    }

    if (executionContext.getEnclaveType() == EnclaveType.REMOTE) {
      ServerConfig enclaveServerConfig = new ServerConfig();
      enclaveServerConfig.setApp(AppType.ENCLAVE);
      SslConfig sslConfig =
          new SslConfig(
              SslAuthenticationMode.STRICT,
              false,
              Paths.get(
                  getClass().getResource("/certificates/server-localhost-with-san.jks").getFile()),
              "testtest".toCharArray(),
              Paths.get(getClass().getResource("/certificates/truststore.jks").getFile()),
              "testtest".toCharArray(),
              SslTrustMode.CA,
              Paths.get(getClass().getResource("/certificates/client.jks").getFile()),
              "testtest".toCharArray(),
              Paths.get(getClass().getResource("/certificates/truststore.jks").getFile()),
              "testtest".toCharArray(),
              SslTrustMode.CA,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);
      enclaveServerConfig.setBindingAddress("http://0.0.0.0:" + enclavePort);
      enclaveServerConfig.setServerAddress("http://localhost:" + enclavePort);
      // enclaveServerConfig.setSslConfig(sslConfig);
      enclaveServerConfig.setCommunicationType(CommunicationType.REST);

      servers.add(enclaveServerConfig);
    }

    config.setServerConfigs(servers);

    if (peerUrls.isEmpty()) {
      config.setPeers(new ArrayList<>());
    }
    peerUrls.stream().map(Peer::new).forEach(config::addPeer);

    config.setKeys(new KeyConfiguration());

    final List<KeyData> pairs =
        keys.entrySet().stream()
            .map(
                e -> {
                  KeyData keyData = new KeyData();
                  keyData.setPublicKey(e.getKey());
                  keyData.setPrivateKey(e.getValue());
                  return keyData;
                })
            .collect(Collectors.toList());

    config.getKeys().setKeyData(pairs);

    config.setAlwaysSendTo(alwaysSendTo);

    config.setFeatures(featureToggles);

    config.setClientMode(executionContext.getClientMode());

    return config;
  }

  public static void main(String... args) throws Exception {

    System.setProperty(
        "jakarta.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
    System.setProperty(
        "jakarta.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

    ExecutionContext executionContext =
        ExecutionContext.Builder.create()
            .with(CommunicationType.REST)
            .with(DBType.H2)
            .with(SocketType.UNIX)
            .with(EnclaveType.REMOTE)
            .build();

    Config config =
        new ConfigBuilder()
            .withExecutionContext(executionContext)
            .withNodeId("mynode")
            .withNodeNumber(1)
            .withPeer("http://localhost:999")
            .withKeys(
                "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=",
                "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=")
            .withQt2Port(999)
            .withP2pPort(888)
            .withEnclavePort(989)
            .build();

    JaxbUtil.marshalWithNoValidation(config, System.out);
  }
}
