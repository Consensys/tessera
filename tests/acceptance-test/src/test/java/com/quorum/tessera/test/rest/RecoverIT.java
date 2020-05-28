package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.SendRequest;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.*;


public class RecoverIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoverIT.class);

    private Map<NodeAlias,ExecManager> executors;

    private SetupDatabase setupDatabase;

    private PartyHelper partyHelper;

    @Before
    public void startNetwork() throws Exception {

        ExecutionContext executionContext =
            ExecutionContext.Builder.create()
                .with(CommunicationType.REST)
                .with(DBType.H2)
                .with(SocketType.HTTP)
                .with(EnclaveType.LOCAL)
                .with(EncryptorType.NACL)
                .prefix(getClass().getSimpleName().toLowerCase())
                .createAndSetupContext();

        partyHelper = PartyHelper.create();

        String nodeId = NodeId.generate(executionContext);
        DatabaseServer databaseServer = executionContext.getDbType().createDatabaseServer(nodeId);
        databaseServer.start();

        setupDatabase = new SetupDatabase(executionContext);
        setupDatabase.setUp();

       this.executors = executionContext.getConfigs().stream()
            .map(NodeExecManager::new)
           .collect(Collectors.toMap(e -> e.getConfigDescriptor().getAlias(),e -> e));

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

        if(!partyInfoSyncLatch.await(2, TimeUnit.MINUTES)) {
            fail("Unable to sync party info");
        }
        executorService.shutdown();

        RestUtils utils = new RestUtils();
        for(int i = 0;i < 10;i++) {
            SendRequest sendRequest = new SendRequest();
            sendRequest.setPayload(utils.createTransactionData());

            Party sender = partyHelper.findByAlias(NodeAlias.A);

            sendRequest.setFrom(sender.getPublicKey());

            List<String> recipients =  partyHelper.getParties()
                .map(Party::getPublicKey)
                .filter(k -> !Objects.equals(k,sender.getPublicKey()))
                .collect(Collectors.toList());

            sendRequest.setTo(recipients.toArray(new String[recipients.size()]));
            sendRequest.setPrivacyFlag(PrivacyMode.STANDARD_PRIVATE.getPrivacyFlag());

           Response response = sender.getRestClientWebTarget()
                .path("send")
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

           assertThat(response.getStatus()).isEqualTo(201);

        }


        Arrays.stream(NodeAlias.values())

            .forEach(a -> {
            long count = doCount(a);
            assertThat(count)
                .describedAs(a + " should have 10 ")
                .isEqualTo(10L);
        });


    }

    @After
    public void stopNetwork() {
        executors.values().forEach(ExecManager::stop);
    }

    @Test
    public void doStuff() throws Exception {

        NodeAlias nodeAlias = NodeAlias.A;

        assertThat(doCount(nodeAlias)).isEqualTo(10L);

        ExecManager execManager = executors.get(nodeAlias);
        execManager.stop();
        setupDatabase.drop(nodeAlias);
        setupDatabase.setUp(nodeAlias);

        assertThat(doCount(nodeAlias)).isZero();

        RecoveryExecManager recoveryExecManager = new RecoveryExecManager(execManager.getConfigDescriptor());
        executors.replace(NodeAlias.A,recoveryExecManager);
        Process process = recoveryExecManager.start();

        assertThat(true).isTrue();

        process.waitFor();


        assertThat(doCount(nodeAlias)).isEqualTo(10L);

    }


    private long doCount(NodeAlias nodeAlias)  {
        Party party = partyHelper.findByAlias(nodeAlias);
        Connection connection = party.getDatabaseConnection();
        try(connection) {

            PreparedStatement preparedStatement
                = connection.prepareStatement("SELECT COUNT(*) FROM ENCRYPTED_TRANSACTION");

            try (preparedStatement) {
                ResultSet resultSet = preparedStatement.executeQuery();
                try(resultSet) {
                    assertThat(resultSet.next()).isTrue();
                    return resultSet.getLong(1);
                }
            }
        }catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }}

}
