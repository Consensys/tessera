package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.test.DBType;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import db.DatabaseServer;
import db.SetupDatabase;
import exec.ExecManager;
import exec.NodeExecManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suite.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class NodeRemoval {

    private List<ExecManager> executors;

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeRemoval.class);

    private PartyHelper partyHelper;

    private ExecutionContext executionContext;

    @Before
    public void onSetup() throws Exception {

        executors = new ArrayList<>();

        EncryptorConfig encryptorConfig = new EncryptorConfig() {{
            setType(EncryptorType.NACL);
        }};

        executionContext =
            ExecutionContext.Builder.create()
                .with(CommunicationType.REST)
                .with(DBType.H2)
                .with(SocketType.HTTP)
                .with(EnclaveType.LOCAL)
                .withAdmin(false)
                .with(encryptorConfig.getType())
                .prefix(getClass().getSimpleName().toLowerCase())
                .createAndSetupContext();


        partyHelper = PartyHelper.create();

        KeyEncryptorFactory.newFactory().create(encryptorConfig);

        String nodeId = NodeId.generate(executionContext);
        DatabaseServer databaseServer = executionContext.getDbType().createDatabaseServer(nodeId);
        databaseServer.start();

        SetupDatabase setupDatabase = new SetupDatabase(executionContext);
        setupDatabase.setUp();

        executionContext.getConfigs().stream()
            .map(NodeExecManager::new)
            .forEach(
                exec -> {
                    exec.start();
                    executors.add(exec);
                });

        checkAllNodesAreRunning();

    }

    @After
    public void onTearDown() throws Exception {
        executors.forEach(ExecManager::stop);
        ExecutionContext.destroyContext();
    }

    private void checkAllNodesAreRunning() throws Exception {
        PartyInfoChecker partyInfoChecker = PartyInfoChecker.create(executionContext.getCommunicationType());

        CountDownLatch partyInfoSyncLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(
            () -> {
                while (!partyInfoChecker.hasSynced()) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ex) {
                    }
                }
                partyInfoSyncLatch.countDown();
            });

        if (!partyInfoSyncLatch.await(2, TimeUnit.MINUTES)) {
            fail("Unable to sync party info nodes");
        }
        executorService.shutdown();
    }

    @Test
    public void transactionPushedToNetworkWithAbsentNodeAndThenComeBackOnline() throws Exception {

        final Client client = ClientBuilder.newClient();

        //Given a 4 node network
        final Party sendingParty = partyHelper.findByAlias("A");
        final Party secondParty = partyHelper.findByAlias("B");
        final Party thirdParty = partyHelper.findByAlias("D");
        final Party forthParty = partyHelper.findByAlias("C");

        final byte[] transactionData = new RestUtils().createTransactionData();

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sendingParty.getPublicKey());
        sendRequest.setTo(secondParty.getPublicKey(), thirdParty.getPublicKey(),forthParty.getPublicKey());
        sendRequest.setPayload(transactionData);

        //And second party goes offline
        ExecManager execManager = executors.get(1);
        executors.remove(execManager);
        execManager.stop();

        //When transaction is send a node
        final Response response =
            client.target(sendingParty.getQ2TUri())
                .path("send")
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        // Then Server returns error
        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getStatusInfo().getReasonPhrase()).contains(secondParty.getP2PUri().toString());


        //When node comes back online
        NodeExecManager nodeExecManager = new NodeExecManager(executionContext.getConfigs().get(1));
        nodeExecManager.start();
        executors.add(nodeExecManager);

        checkAllNodesAreRunning();

        //And transaction is resent
        final Response response2 =
            client.target(sendingParty.getQ2TUri())
                .path("send")
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        //Then transaction is published to network
        assertThat(response2.getStatus()).isEqualTo(201);

    }



}
