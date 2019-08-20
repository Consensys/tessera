package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.DBType;
import com.quorum.tessera.test.Party;
import config.ConfigDescriptor;
import exec.EnclaveExecManager;
import exec.NodeExecManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import suite.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class SendWithRemoteEnclaveReconnectIT {

    private EnclaveExecManager enclaveExecManager;

    private NodeExecManager nodeExecManager;

    private ConfigDescriptor configDescriptor;

    private Party party;

    @Before
    public void onSetup() throws IOException {

        ExecutionContext.Builder.create()
                .with(CommunicationType.REST)
                .with(DBType.H2)
                .with(SocketType.HTTP)
                .with(EnclaveType.REMOTE)
                .buildAndStoreContext();

        final AtomicInteger portGenerator = new AtomicInteger(50100);

        final String serverUriTemplate = "http://localhost:%d";

        final Config nodeConfig = new Config();

        final JdbcConfig jdbcConfig = new JdbcConfig("sa", "", "jdbc:h2:mem:junit");
        jdbcConfig.setAutoCreateTables(true);
        nodeConfig.setJdbcConfig(jdbcConfig);

        ServerConfig p2pServerConfig = new ServerConfig();
        p2pServerConfig.setApp(AppType.P2P);
        p2pServerConfig.setEnabled(true);
        p2pServerConfig.setServerAddress(String.format(serverUriTemplate, portGenerator.incrementAndGet()));
        p2pServerConfig.setCommunicationType(CommunicationType.REST);

        final ServerConfig q2tServerConfig = new ServerConfig();
        q2tServerConfig.setApp(AppType.Q2T);
        q2tServerConfig.setEnabled(true);
        q2tServerConfig.setServerAddress(String.format(serverUriTemplate, portGenerator.incrementAndGet()));
        q2tServerConfig.setCommunicationType(CommunicationType.REST);

        final Config enclaveConfig = new Config();

        final ServerConfig enclaveServerConfig = new ServerConfig();
        enclaveServerConfig.setApp(AppType.ENCLAVE);
        enclaveServerConfig.setEnabled(true);
        enclaveServerConfig.setServerAddress(String.format(serverUriTemplate, portGenerator.incrementAndGet()));
        enclaveServerConfig.setCommunicationType(CommunicationType.REST);

        nodeConfig.setServerConfigs(Arrays.asList(p2pServerConfig, q2tServerConfig, enclaveServerConfig));

        DirectKeyPair keyPair =
                new DirectKeyPair(
                        "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");

        enclaveConfig.setKeys(new KeyConfiguration());
        enclaveConfig.getKeys().setKeyData(new ArrayList<>(Arrays.asList(keyPair)));

        nodeConfig.setPeers(Arrays.asList(new Peer(p2pServerConfig.getServerAddress())));

        enclaveConfig.setServerConfigs(Arrays.asList(enclaveServerConfig));

        Path configPath = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
        Path enclaveConfigPath = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");

        this.writeConfig(nodeConfig, configPath);
        this.writeConfig(enclaveConfig, enclaveConfigPath);

        this.configDescriptor =
                new ConfigDescriptor(NodeAlias.A, configPath, nodeConfig, enclaveConfig, enclaveConfigPath);

        String key = configDescriptor.getKey().getPublicKey();
        URL file = Utils.toUrl(configDescriptor.getPath());
        String alias = configDescriptor.getAlias().name();

        this.party = new Party(key, file, alias);

        nodeExecManager = new NodeExecManager(configDescriptor);
        enclaveExecManager = new EnclaveExecManager(configDescriptor);

        enclaveExecManager.start();
        nodeExecManager.start();
    }

    @After
    public void onTearDown() {
        nodeExecManager.stop();
        enclaveExecManager.stop();
        ExecutionContext.destroyContext();
    }

    @Test
    public void sendTransactionToSelfWhenEnclaveIsDown() {

        enclaveExecManager.stop();

        RestUtils utils = new RestUtils();
        byte[] transactionData = utils.createTransactionData();
        final SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(party.getPublicKey());
        sendRequest.setPayload(transactionData);

        Client client = ClientBuilder.newClient();

        final Response response =
                client.target(party.getQ2TUri())
                        .path("send")
                        .request()
                        .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(503);

        this.enclaveExecManager = new EnclaveExecManager(this.configDescriptor);
        enclaveExecManager.start();

        final Response secondresponse =
                client.target(party.getQ2TUri())
                        .path("send")
                        .request()
                        .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(secondresponse.getStatus()).isEqualTo(201);
    }

    @Test
    public void reconnectingToEnclaveUpdatesKeys() throws IOException {
        enclaveExecManager.stop();

        final DirectKeyPair addedKeypair =
                new DirectKeyPair(
                        "sL/prFZhfUNhbL+7Ky7bHA+OEBhqty0L+PaOuA0bj1M=", "JIQ3a2udSn+xxfhM5pQP+sn3u9BblC84Clpk5tsYmg4=");

        final Config enclaveConfig = this.configDescriptor.getEnclaveConfig().get();
        enclaveConfig.getKeys().getKeyData().add(addedKeypair);
        this.writeConfig(enclaveConfig, this.configDescriptor.getEnclavePath());

        this.enclaveExecManager = new EnclaveExecManager(this.configDescriptor);
        enclaveExecManager.start();

        Client client = ClientBuilder.newClient();

        final Response response = client.target(party.getP2PUri()).path("partyinfo").request().get();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    private void writeConfig(final Config config, final Path outputPath) throws IOException {
        try (OutputStream out = Files.newOutputStream(outputPath)) {
            JaxbUtil.marshalWithNoValidation(config, out);
            out.flush();
        }
    }
}
