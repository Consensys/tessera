package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.DBType;
import com.quorum.tessera.test.Party;
import config.ConfigDescriptor;
import config.PortUtil;
import exec.EnclaveExecManager;
import exec.NodeExecManager;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suite.EnclaveType;
import suite.ExecutionContext;
import suite.NodeAlias;
import suite.SocketType;
import suite.Utils;

public class SendWithRemoteEnclaveReconnectIT {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SendWithRemoteEnclaveReconnectIT.class);

  private EnclaveExecManager enclaveExecManager;

  private NodeExecManager nodeExecManager;

  private Party party;

  private Client client;

  @Before
  public void beforeTest() throws IOException {

    EncryptorConfig encryptorConfig =
        new EncryptorConfig() {
          {
            setType(EncryptorType.NACL);
          }
        };

    ExecutionContext.Builder.create()
        .with(CommunicationType.REST)
        .with(DBType.H2)
        .with(SocketType.HTTP)
        .with(EnclaveType.REMOTE)
        .with(encryptorConfig.getType())
        .with(ClientMode.TESSERA)
        .buildAndStoreContext();

    final PortUtil portGenerator = new PortUtil(50100);

    final String serverUriTemplate = "http://localhost:%d";

    KeyEncryptorFactory.newFactory().create(encryptorConfig);

    final Config nodeConfig = new Config();
    nodeConfig.setEncryptor(encryptorConfig);

    JdbcConfig jdbcConfig = new JdbcConfig();
    jdbcConfig.setUrl("jdbc:h2:mem:junit");
    jdbcConfig.setUsername("sa");
    jdbcConfig.setPassword("");
    jdbcConfig.setAutoCreateTables(true);
    nodeConfig.setJdbcConfig(jdbcConfig);

    ServerConfig p2pServerConfig = new ServerConfig();
    p2pServerConfig.setApp(AppType.P2P);
    p2pServerConfig.setServerAddress(String.format(serverUriTemplate, portGenerator.nextPort()));
    p2pServerConfig.setCommunicationType(CommunicationType.REST);

    final ServerConfig q2tServerConfig = new ServerConfig();
    q2tServerConfig.setApp(AppType.Q2T);
    q2tServerConfig.setServerAddress(String.format(serverUriTemplate, portGenerator.nextPort()));
    q2tServerConfig.setCommunicationType(CommunicationType.REST);

    final Config enclaveConfig = new Config();
    enclaveConfig.setEncryptor(nodeConfig.getEncryptor());

    final ServerConfig enclaveServerConfig = new ServerConfig();
    enclaveServerConfig.setApp(AppType.ENCLAVE);
    enclaveServerConfig.setServerAddress(
        String.format(serverUriTemplate, portGenerator.nextPort()));
    enclaveServerConfig.setCommunicationType(CommunicationType.REST);

    nodeConfig.setServerConfigs(
        Arrays.asList(p2pServerConfig, q2tServerConfig, enclaveServerConfig));

    KeyData keyPair = new KeyData();
    keyPair.setPublicKey("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    keyPair.setPrivateKey("yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");

    enclaveConfig.setKeys(new KeyConfiguration());
    enclaveConfig.getKeys().setKeyData(Arrays.asList(keyPair));

    nodeConfig.setPeers(Arrays.asList(new Peer(p2pServerConfig.getServerAddress())));

    enclaveConfig.setServerConfigs(Arrays.asList(enclaveServerConfig));

    Path configPath = Files.createFile(Paths.get(UUID.randomUUID().toString()));
    configPath.toFile().deleteOnExit();

    Path enclaveConfigPath = Files.createFile(Paths.get(UUID.randomUUID().toString()));
    enclaveConfigPath.toFile().deleteOnExit();

    try (OutputStream out = Files.newOutputStream(configPath)) {
      JaxbUtil.marshalWithNoValidation(nodeConfig, out);
      out.flush();
    }

    JaxbUtil.marshalWithNoValidation(enclaveConfig, System.out);
    try (OutputStream out = Files.newOutputStream(enclaveConfigPath)) {
      JaxbUtil.marshalWithNoValidation(enclaveConfig, out);
      out.flush();
    }
    ConfigDescriptor configDescriptor =
        new ConfigDescriptor(NodeAlias.A, configPath, nodeConfig, enclaveConfig, enclaveConfigPath);

    String key = configDescriptor.getKey().getPublicKey();
    URL file = Utils.toUrl(configDescriptor.getPath());
    String alias = configDescriptor.getAlias().name();

    this.party = new Party(key, file, alias);

    nodeExecManager = new NodeExecManager(configDescriptor);
    enclaveExecManager = new EnclaveExecManager(configDescriptor);

    enclaveExecManager.start();

    nodeExecManager.start();

    client = party.getRestClient();
  }

  @After
  public void afterTest() {
    try {
      nodeExecManager.stop();
      enclaveExecManager.stop();
      client.close();
    } finally {
      ExecutionContext.destroyContext();
    }
  }

  @Test
  public void sendTransactiuonToSelfWhenEnclaveIsDown() throws InterruptedException {
    LOGGER.info("Stopping Enclave node");
    enclaveExecManager.stop();
    LOGGER.info("Stopped Enclave node");

    RestUtils utils = new RestUtils();
    byte[] transactionData = utils.createTransactionData();
    final SendRequest sendRequest = new SendRequest();
    sendRequest.setFrom(party.getPublicKey());
    sendRequest.setPayload(transactionData);

    final Response response =
        client
            .target(party.getQ2TUri())
            .path("send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(503);

    //        LOGGER.info("Starting Enclave node");
    //        enclaveExecManager.start();
    //        LOGGER.info("Started Enclave node");
    //
    //        final Response secondresponse =
    //                client.target(party.getQ2TUri())
    //                        .path("send")
    //                        .request()
    //                        .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));
    //
    //        assertThat(secondresponse.getStatus()).isEqualTo(201);
  }
}
