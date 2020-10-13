package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.test.DBType;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import db.DatabaseServer;
import db.SetupDatabase;
import db.UncheckedSQLException;
import exec.ExecManager;
import exec.NodeExecManager;
import exec.RecoveryExecManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suite.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.*;

public class RecoverIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoverIT.class);

    private Map<NodeAlias, ExecManager> executors;

    private SetupDatabase setupDatabase;

    private PartyHelper partyHelper;

    private Party sender;

    private List<Party> recipients;

    @Before
    public void startNetwork() throws Exception {
        final ExecutionContext executionContext =
                ExecutionContext.Builder.create()
                        .with(CommunicationType.REST)
                        .with(DBType.H2)
                        .with(SocketType.HTTP)
                        .with(EnclaveType.LOCAL)
                        .with(EncryptorType.NACL)
                        .prefix(RecoverIT.class.getSimpleName().toLowerCase())
                        .createAndSetupContext();

        partyHelper = PartyHelper.create();
        sender = partyHelper.findByAlias(NodeAlias.A);
        recipients =
                partyHelper
                        .getParties()
                        .filter(Predicate.not(p -> p.getAlias().equals(sender.getAlias())))
                        .collect(Collectors.toList());

        String nodeId = NodeId.generate(executionContext);
        DatabaseServer databaseServer = executionContext.getDbType().createDatabaseServer(nodeId);
        databaseServer.start();

        setupDatabase = new SetupDatabase(executionContext);
        setupDatabase.setUp();

        this.executors =
                executionContext.getConfigs().stream()
                        .map(NodeExecManager::new)
                        .collect(Collectors.toMap(e -> e.getConfigDescriptor().getAlias(), e -> e));

        executors.values().forEach(ExecManager::start);

        partyInfoSync();

        sendTransactions();

        Arrays.stream(NodeAlias.values())
                .forEach(
                        a -> {
                            long count = doCount(a);
                            if (a == NodeAlias.D) {
                                assertThat(count).describedAs(a + " should have 100 ").isEqualTo(100L);
                            } else {
                                assertThat(count).describedAs(a + " should have 500 ").isEqualTo(500L);
                            }
                        });
    }

    @After
    public void stopNetwork() throws Exception {
        setupDatabase.dropAll();
        ExecutionContext.destroyContext();
        executors.values().forEach(ExecManager::stop);
    }

    void sendTransactions() {

        final List<String> recipientList = recipients.stream().map(Party::getPublicKey).collect(Collectors.toList());

        final List<String> privacyRecipients =
                recipients.stream()
                        .filter(r -> r != partyHelper.findByAlias(NodeAlias.D))
                        .map(Party::getPublicKey)
                        .collect(Collectors.toList());

        // Creating SP Contract
        final String spOriginalHash = sendTransaction(PrivacyMode.STANDARD_PRIVATE, new String[0], recipientList);
        for (int i = 0; i < 99; i++) {
            sendTransaction(PrivacyMode.STANDARD_PRIVATE, new String[] {spOriginalHash}, recipientList);
        }

        // Creating PP Contract
        final String ppOriginalHash = sendTransaction(PrivacyMode.PARTY_PROTECTION, new String[0], privacyRecipients);
        for (int i = 0; i < 199; i++) {
            sendTransaction(PrivacyMode.PARTY_PROTECTION, new String[] {ppOriginalHash}, privacyRecipients);
        }

        // Creating PSV Contract
        final String psvOriginalHash =
            sendTransaction(PrivacyMode.PRIVATE_STATE_VALIDATION, new String[0], privacyRecipients);
        for (int i = 0; i < 199; i++) {
            sendTransaction(PrivacyMode.PRIVATE_STATE_VALIDATION, new String[] {psvOriginalHash}, privacyRecipients);
        }
    }

    @Test
    public void recoverNodes() throws Exception {

        List<NodeAlias> aliases = Arrays.asList(NodeAlias.values());
        Collections.shuffle(aliases);

        for (NodeAlias nodeAlias : aliases) {
            recoverNode(nodeAlias);
        }

    }

    private void recoverNode(NodeAlias nodeAlias) throws Exception {

        ExecManager execManager = executors.get(nodeAlias);
        execManager.stop();
        setupDatabase.drop(nodeAlias);
        setupDatabase.setUp(nodeAlias);

        assertThat(doCount(nodeAlias)).isZero();

        RecoveryExecManager recoveryExecManager = new RecoveryExecManager(execManager.getConfigDescriptor());

        Process process = recoveryExecManager.start();

        assertThat(true).isTrue();

        process.waitFor();

        if (nodeAlias == NodeAlias.D) {
            assertThat(doCount(nodeAlias)).isEqualTo(100);
        } else {
            assertThat(doCount(nodeAlias)).isEqualTo(500);
        }

        recoveryExecManager.stop();

        NodeExecManager nodeExecManager = new NodeExecManager(execManager.getConfigDescriptor());

        nodeExecManager.start();

        partyInfoSync();

        executors.replace(nodeAlias, nodeExecManager);
    }

    private long doCount(NodeAlias nodeAlias) {
        Party party = partyHelper.findByAlias(nodeAlias);
        Connection connection = party.getDatabaseConnection();

        try (connection) {

            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT COUNT(*) FROM ENCRYPTED_TRANSACTION");

            try (preparedStatement) {
                ResultSet resultSet = preparedStatement.executeQuery();
                try (resultSet) {
                    assertThat(resultSet.next()).isTrue();
                    return resultSet.getLong(1);
                }
            }
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    private String sendTransaction(PrivacyMode privacyMode, String[] affectedHash, List<String> recipients) {

        Party sender = partyHelper.findByAlias(NodeAlias.A);

        SendRequest sendRequest = new SendRequest();
        sendRequest.setPayload(new RestUtils().createTransactionData());
        sendRequest.setFrom(sender.getPublicKey());
        sendRequest.setTo(recipients.toArray(new String[recipients.size()]));
        sendRequest.setPrivacyFlag(privacyMode.getPrivacyFlag());
        sendRequest.setAffectedContractTransactions(affectedHash);
        if (privacyMode == PrivacyMode.PRIVATE_STATE_VALIDATION) {
            sendRequest.setExecHash("execHash");
        }

        Response response =
                sender.getRestClientWebTarget()
                        .path("send")
                        .request()
                        .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(201);
        final SendResponse result = response.readEntity(SendResponse.class);

        return result.getKey();
    }

    private void partyInfoSync() throws InterruptedException {
        PartyInfoChecker partyInfoChecker = PartyInfoChecker.create(CommunicationType.REST);

        final CountDownLatch partyInfoSyncLatch = new CountDownLatch(1);
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
            fail("Unable to sync party info");
        }
        executorService.shutdown();
    }
}
