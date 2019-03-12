package config;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.DBType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import suite.ExecutionContext;
import suite.SocketType;

public class ConfigBuilder {

    private Integer q2tPort;

    private Integer p2pPort;

    private Integer adminPort;

    private ExecutionContext executionContext;

    private String nodeId;

    private Integer nodeNumber;

    private List<String> peerUrls = new ArrayList<>();

    private Map<String, String> keys = new HashMap<>();

    private List<String> alwaysSendTo = new ArrayList<>();

    private SocketType q2tSocketType;

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

    public ConfigBuilder withNodeNumbber(Integer nodeNumber) {
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

    public ConfigBuilder withExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
        return this;
    }

    public Config build() {
        final Config config = new Config();

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
        p2pServerConfig.setCommunicationType(executionContext.getCommunicationType());
        p2pServerConfig.setServerAddress("http://localhost:" + p2pPort);
        p2pServerConfig.setBindingAddress("http://0.0.0.0:" + p2pPort);
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
        config.setServerConfigs(servers);

        peerUrls.stream()
                .map(Peer::new).forEach(config::addPeer);

        config.setKeys(new KeyConfiguration());
        config.getKeys().setKeyData(new ArrayList<>());

        keys.entrySet().stream().map(e -> new DirectKeyPair(e.getKey(), e.getValue())).forEach(v -> {
            config.getKeys().getKeyData().add(v);
        });

        config.setAlwaysSendTo(alwaysSendTo);

        return config;
    }

    public static void main(String... args) throws Exception {

        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        ExecutionContext executionContext = ExecutionContext.Builder.create()
                .with(CommunicationType.REST)
                .with(DBType.H2)
                .with(SocketType.UNIX)
                .build();

        Config config = new ConfigBuilder()
                .withExecutionContext(executionContext)
                .withNodeId("mynode")
                .withNodeNumbber(1)
                .withPeer("http://localhost:999")
                .withKeys("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=")
                .withQt2Port(999)
                .withP2pPort(888)
                .build();

        JaxbUtil.marshalWithNoValidation(config, System.out);
    }

}
