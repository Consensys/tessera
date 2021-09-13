package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.test.DBType;
import config.ConfigDescriptor;
import config.PortUtil;
import exec.NodeExecManager;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.*;
import suite.EnclaveType;
import suite.ExecutionContext;
import suite.NodeAlias;
import suite.SocketType;

public class ThirdPartyIT {

  private static final PortUtil PORT_UTIL = new PortUtil(50100);

  private static NodeExecManager firstNodeExecManager;

  private static NodeExecManager secondNodeExecManager;

  private ServerConfig thirdPartyServerConfig;

  private Client client;

  @Before
  public void beforeTest() {
    thirdPartyServerConfig =
        secondNodeExecManager.getConfigDescriptor().getConfig().getServerConfigs().stream()
            .filter(s -> s.getApp() == AppType.THIRD_PARTY)
            .findFirst()
            .get();

    client = new ClientFactory().buildFrom(thirdPartyServerConfig);
  }

  @After
  public void afterTest() {
    client.close();
  }

  @BeforeClass
  public static void init() throws Exception {

    ExecutionContext.Builder.create()
        .with(CommunicationType.REST)
        .with(DBType.H2)
        .with(SocketType.HTTP)
        .with(EncryptorType.NACL)
        .with(EnclaveType.LOCAL)
        .with(ClientMode.TESSERA)
        .prefix(ThirdPartyIT.class.getSimpleName().toLowerCase())
        .buildAndStoreContext();

    Config firstNodeDesc = createNode(NodeAlias.A);
    Config secondNodeDesc = createNode(NodeAlias.B);

    firstNodeDesc.setPeers(
        List.of(new Peer(secondNodeDesc.getP2PServerConfig().getServerAddress())));
    secondNodeDesc.setPeers(
        List.of(new Peer(firstNodeDesc.getP2PServerConfig().getServerAddress())));

    firstNodeExecManager = new NodeExecManager(createConfigDescriptor(NodeAlias.A, firstNodeDesc));
    secondNodeExecManager =
        new NodeExecManager(createConfigDescriptor(NodeAlias.B, secondNodeDesc));
    firstNodeExecManager.start();
    secondNodeExecManager.start();
  }

  static ConfigDescriptor createConfigDescriptor(NodeAlias nodeAlias, Config config)
      throws Exception {
    Path dir = Paths.get("build", "thirdpty", "node".concat(nodeAlias.name()));
    dir.toFile().deleteOnExit();
    Files.createDirectories(dir);
    Path configPath = dir.resolve("config.json");
    try (OutputStream out = Files.newOutputStream(configPath)) {
      JaxbUtil.marshalWithNoValidation(config, out);
      out.flush();
    }
    return new ConfigDescriptor(nodeAlias, configPath, config, null, null);
  }

  static Config createNode(NodeAlias nodeAlias) {

    Config config = new Config();
    config.setEncryptor(
        new EncryptorConfig() {
          {
            setType(EncryptorType.NACL);
          }
        });
    config.setJdbcConfig(
        new JdbcConfig() {
          {
            setUsername("junit");
            setPassword("junit");
            setUrl("jdbc:h2:mem:thirdpty".concat(nodeAlias.name()));
            setAutoCreateTables(true);
          }
        });

    config.setKeys(new KeyConfiguration());

    KeyData keyPair = new KeyData();
    keyPair.setPublicKey("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    keyPair.setPrivateKey("yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");

    config.getKeys().setKeyData(List.of(keyPair));

    final String serverUriTemplate = "http://localhost:%d";

    config.setServerConfigs(
        List.of(
            new ServerConfig() {
              {
                setApp(AppType.THIRD_PARTY);
                setServerAddress(String.format(serverUriTemplate, PORT_UTIL.nextPort()));
              }
            },
            new ServerConfig() {
              {
                setApp(AppType.P2P);
                setServerAddress(String.format(serverUriTemplate, PORT_UTIL.nextPort()));
              }
            },
            new ServerConfig() {
              {
                setApp(AppType.Q2T);
                setServerAddress(String.format(serverUriTemplate, PORT_UTIL.nextPort()));
              }
            }));

    return config;
  }

  @AfterClass
  public static void destroy() {
    try {
      firstNodeExecManager.stop();
      secondNodeExecManager.stop();
    } finally {
      ExecutionContext.destroyContext();
    }
  }

  @Test
  public void partyInfoKeys() {

    Response partyinfoResponse =
        client
            .target(thirdPartyServerConfig.getServerUri())
            .path("partyinfo")
            .path("keys")
            .request()
            .get();

    JsonObject partyinfokeysJson = partyinfoResponse.readEntity(JsonObject.class);

    assertThat(partyinfoResponse).isNotNull();
    assertThat(partyinfoResponse.getStatus()).isEqualTo(200);

    List<JsonObject> keys =
        Stream.of(firstNodeExecManager, secondNodeExecManager)
            .map(NodeExecManager::getConfigDescriptor)
            .map(ConfigDescriptor::getKey)
            .map(ConfigKeyPair::getPublicKey)
            .map(k -> Json.createObjectBuilder().add("key", k).build())
            .collect(Collectors.toUnmodifiableList());

    assertThat(partyinfokeysJson.getJsonArray("keys"))
        .describedAs("partyInfo response that caused failure %s", partyinfokeysJson.toString())
        .containsAnyElementsOf(keys);
  }

  @Test
  public void keys() {

    Response response =
        client.target(thirdPartyServerConfig.getServerUri()).path("keys").request().get();
    JsonObject keysJson = response.readEntity(JsonObject.class);

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(keysJson).isNotNull();
    assertThat(keysJson.getJsonArray("keys"))
        .hasSize(1)
        .containsExactly(
            Json.createObjectBuilder()
                .add("key", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
                .build());
  }
}
