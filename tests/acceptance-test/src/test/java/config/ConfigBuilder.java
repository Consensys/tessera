package config;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.ssl.context.ClientSSLContextFactory;
import com.quorum.tessera.test.DBType;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import com.quorum.tessera.server.websockets.ExtendedJettyClientContainerProvider;
import suite.EnclaveType;
import suite.ExecutionContext;
import suite.SocketType;

public class ConfigBuilder {

    private final SslConfig sslConfig =
            new SslConfig(
                    SslAuthenticationMode.STRICT,
                    false,
                    Paths.get(getClass().getResource("/certificates/localhost-with-san-keystore.jks").getFile()),
                    "testtest",
                    Paths.get(getClass().getResource("/certificates/truststore.jks").getFile()),
                    "testtest",
                    SslTrustMode.CA,
                    Paths.get(getClass().getResource("/certificates/quorum-client-keystore.jks").getFile())
                            .toAbsolutePath(),
                    "testtest",
                    Paths.get(getClass().getResource("/certificates/truststore.jks").getFile()),
                    "testtest",
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

    private Integer adminPort;

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

    public ConfigBuilder withAdminPort(Integer adminPort) {
        this.adminPort = adminPort;
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
        jdbcConfig.setPassword("");
        config.setJdbcConfig(jdbcConfig);

        ServerConfig q2tServerConfig = new ServerConfig();
        q2tServerConfig.setApp(AppType.Q2T);
        q2tServerConfig.setEnabled(true);
        q2tServerConfig.setCommunicationType(executionContext.getCommunicationType());

        if (executionContext.getCommunicationType() == CommunicationType.REST
                && (q2tSocketType != null || executionContext.getSocketType() == SocketType.UNIX)) {
            q2tServerConfig.setServerAddress(String.format("unix:/tmp/q2t-rest-unix-%d.ipc", nodeNumber));
        } else {
            q2tServerConfig.setServerAddress("http://localhost:" + q2tPort);
            q2tServerConfig.setBindingAddress("http://0.0.0.0:" + q2tPort);
        }

        List<ServerConfig> servers = new ArrayList<>();

        servers.add(q2tServerConfig);

        ServerConfig p2pServerConfig = new ServerConfig();
        p2pServerConfig.setApp(AppType.P2P);
        p2pServerConfig.setEnabled(true);

        p2pServerConfig.setCommunicationType(executionContext.getP2pCommunicationType());

        if (executionContext.isP2pSsl()) {
            p2pServerConfig.setSslConfig(sslConfig);
        }

        if (executionContext.getP2pCommunicationType() == CommunicationType.WEB_SOCKET) {

            final String scheme;
            if (executionContext.isP2pSsl()) {

                java.net.URI uri =
                        UriBuilder.fromUri("wss://localhost").scheme("wss").host("localhost").port(p2pPort).build();

                SSLContext sslContext = ClientSSLContextFactory.create().from(uri.toString(), sslConfig);

                ExtendedJettyClientContainerProvider.setSslContext(sslContext);
                ExtendedJettyClientContainerProvider.useSingleton(true);

                scheme = "wss";
            } else {
                scheme = "ws";
            }

            ExtendedJettyClientContainerProvider.configured();

            // p2pServerConfig.setSslConfig(sslConfig);

            p2pServerConfig.setServerAddress(scheme + "://localhost:" + p2pPort);
            p2pServerConfig.setBindingAddress(scheme + "://0.0.0.0:" + p2pPort);
        } else {
            final String scheme = executionContext.isP2pSsl() ? "https" : "http";

            p2pServerConfig.setServerAddress(scheme + "://localhost:" + p2pPort);
            p2pServerConfig.setBindingAddress(scheme + "://0.0.0.0:" + p2pPort);
        }

        servers.add(p2pServerConfig);

        if (executionContext.getCommunicationType() == CommunicationType.REST && Objects.nonNull(adminPort)) {
            ServerConfig adminServerConfig = new ServerConfig();
            adminServerConfig.setApp(AppType.ADMIN);
            adminServerConfig.setEnabled(true);
            adminServerConfig.setServerAddress("http://localhost:" + adminPort);
            adminServerConfig.setBindingAddress("http://0.0.0.0:" + adminPort);
            adminServerConfig.setCommunicationType(CommunicationType.REST);

            servers.add(adminServerConfig);
        }

        if (executionContext.getEnclaveType() == EnclaveType.REMOTE) {
            ServerConfig enclaveServerConfig = new ServerConfig();
            enclaveServerConfig.setApp(AppType.ENCLAVE);
            enclaveServerConfig.setEnabled(true);

            enclaveServerConfig.setBindingAddress("http://0.0.0.0:" + enclavePort);
            enclaveServerConfig.setServerAddress("http://localhost:" + enclavePort);
            // enclaveServerConfig.setSslConfig(sslConfig);
            enclaveServerConfig.setCommunicationType(CommunicationType.REST);

            servers.add(enclaveServerConfig);
        }

        config.setServerConfigs(servers);

        peerUrls.stream().map(Peer::new).forEach(config::addPeer);

        config.setKeys(new KeyConfiguration());

        final List<ConfigKeyPair> pairs =
                keys.entrySet().stream()
                        .map(e -> new DirectKeyPair(e.getKey(), e.getValue()))
                        .collect(Collectors.toList());

        config.getKeys().setKeyData(pairs);

        config.setAlwaysSendTo(alwaysSendTo);

        config.setFeatures(featureToggles);

        return config;
    }

    public static void main(String... args) throws Exception {

        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

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
