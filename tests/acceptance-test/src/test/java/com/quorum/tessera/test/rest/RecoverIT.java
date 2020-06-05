package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
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

@RunWith(Parameterized.class)
public class RecoverIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoverIT.class);

    private Map<NodeAlias, ExecManager> executors;

    private SetupDatabase setupDatabase;

    private PartyHelper partyHelper;

    private static final long TXN_COUNT = 10;

    private Party sender;

    private List<Party> recipients;

    private final PrivacyMode privacyMode;

    public RecoverIT(PrivacyMode privacyMode) {
        this.privacyMode = privacyMode;
    }

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

        PartyInfoChecker partyInfoChecker = PartyInfoChecker.create(executionContext.getCommunicationType());

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

        sendTxns(privacyMode);

        Arrays.stream(NodeAlias.values())
                .forEach(
                        a -> {
                            long count = doCount(a);
                            assertThat(count).describedAs(a + " should have 10 ").isEqualTo(10L);
                        });
    }

    @After
    public void stopNetwork() throws Exception {
        ExecutionContext.destroyContext();
        executors.values().forEach(ExecManager::stop);
        setupDatabase.dropAll();
    }

    byte[] createPayload(PrivacyMode privacyMode) {

        if (privacyMode == PrivacyMode.STANDARD_PRIVATE) {
            return new RestUtils().createTransactionData();
        }
        PublicKey senderKey = extractKey(sender);

        List<PublicKey> recipientKeys = recipients.stream().map(RecoverIT::extractKey).collect(Collectors.toList());

        PayloadEncoder payloadEncoder = PayloadEncoder.create();
        EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withRecipientKeys(recipientKeys)
                        .withPrivacyMode(privacyMode)
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHER".getBytes())
                        .withCipherTextNonce("NONCE".getBytes())
                        .withRecipientNonce("RECIPIENT_NONCE".getBytes())
                        .build();
        return payloadEncoder.encode(encodedPayload);
    }

    static PublicKey extractKey(Party party) {
        return Optional.of(party).map(Party::getPublicKey).map(Base64.getDecoder()::decode).map(PublicKey::from).get();
    }

    void sendTxns(PrivacyMode privacyMode) {

        for (int i = 0; i < TXN_COUNT; i++) {
            SendRequest sendRequest = new SendRequest();

            sendRequest.setPayload(createPayload(privacyMode));
            sendRequest.setFrom(sender.getPublicKey());

            List<String> recipientList = recipients.stream().map(Party::getPublicKey).collect(Collectors.toList());

            sendRequest.setTo(recipientList.toArray(new String[recipientList.size()]));
            sendRequest.setPrivacyFlag(privacyMode.getPrivacyFlag());

            Response response =
                    sender.getRestClientWebTarget()
                            .path("send")
                            .request()
                            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

            assertThat(response.getStatus()).isEqualTo(201);
        }
    }

    @Test
    public void doStuff() throws Exception {

        List<NodeAlias> aliases = Arrays.asList(NodeAlias.values());
        Collections.shuffle(aliases);

        for (NodeAlias nodeAlias : aliases) {
            assertThat(doCount(nodeAlias)).isEqualTo(TXN_COUNT);

            recoverNode(nodeAlias);
        }
    }

    void recoverNode(NodeAlias nodeAlias) throws Exception {
        assertThat(doCount(nodeAlias)).isEqualTo(TXN_COUNT);

        ExecManager execManager = executors.get(nodeAlias);
        execManager.stop();
        setupDatabase.drop(nodeAlias);
        setupDatabase.setUp(nodeAlias);

        assertThat(doCount(nodeAlias)).isZero();

        RecoveryExecManager recoveryExecManager = new RecoveryExecManager(execManager.getConfigDescriptor());

        Process process = recoveryExecManager.start();

        assertThat(true).isTrue();

        process.waitFor();

        assertThat(doCount(nodeAlias)).isEqualTo(TXN_COUNT);

        recoveryExecManager.stop();

        NodeExecManager nodeExecManager = new NodeExecManager(execManager.getConfigDescriptor());

        nodeExecManager.start();

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

    @Parameterized.Parameters(name = "Private mode : {0}")
    public static Collection<PrivacyMode> privacyModes() {
        return Arrays.asList(PrivacyMode.values());
    }
}
