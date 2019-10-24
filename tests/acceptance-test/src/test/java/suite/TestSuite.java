package suite;

import db.DatabaseServer;
import db.SetupDatabase;
import exec.EnclaveExecManager;
import exec.ExecManager;
import exec.NodeExecManager;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSuite extends Suite {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSuite.class);

    private ProcessConfiguration testConfig;

    public TestSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
    }

    public void withConfiguration(final ProcessConfiguration parameterizedConfig) {
        this.testConfig = parameterizedConfig;
    }

    @Override
    public void run(RunNotifier notifier) {

        final List<ExecManager> executors = new ArrayList<>();
        try {

            if (testConfig == null) {

                final ProcessConfig annotatedConfig =
                        Arrays.stream(getRunnerAnnotations())
                                .filter(ProcessConfig.class::isInstance)
                                .map(ProcessConfig.class::cast)
                                .findAny()
                                .orElseThrow(() -> new AssertionError("No Test config found"));

                com.quorum.tessera.config.CommunicationType p2pCommType =
                        Optional.of(annotatedConfig.p2pCommunicationType())
                                .filter(v -> !Objects.equals("NONE", v))
                                .map(com.quorum.tessera.config.CommunicationType::valueOf)
                                .orElse(annotatedConfig.communicationType());

                this.testConfig =
                        new ProcessConfiguration(
                                annotatedConfig.dbType(),
                                annotatedConfig.communicationType(),
                                annotatedConfig.socketType(),
                                annotatedConfig.enclaveType(),
                                annotatedConfig.admin(),
                                annotatedConfig.prefix(),
                                annotatedConfig.p2pSsl(),
                                annotatedConfig.encryptorType());

                this.testConfig.setP2pCommunicationType(p2pCommType);
            }

            ExecutionContext executionContext =
                    ExecutionContext.Builder.create()
                            .with(testConfig.getCommunicationType())
                            .with(testConfig.getDbType())
                            .with(testConfig.getSocketType())
                            .with(testConfig.getEnclaveType())
                            .withP2pCommunicationType(testConfig.getP2pCommunicationType())
                            .withAdmin(testConfig.isAdmin())
                            .prefix(testConfig.getPrefix())
                            .withP2pSsl(testConfig.isP2pSsl())
                            .with(testConfig.getEncryptorType())
                            .createAndSetupContext();

            if (executionContext.getEnclaveType() == EnclaveType.REMOTE) {

                executionContext.getConfigs().stream()
                        .map(EnclaveExecManager::new)
                        .forEach(
                                exec -> {
                                    exec.start();
                                    executors.add(exec);
                                });
            }

            String nodeId = NodeId.generate(executionContext);
            DatabaseServer databaseServer = testConfig.getDbType().createDatabaseServer(nodeId);
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

            PartyInfoChecker partyInfoChecker = PartyInfoChecker.create(executionContext.getP2pCommunicationType());

            CountDownLatch partyInfoSyncLatch = new CountDownLatch(1);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(
                    () -> {
                        while (!partyInfoChecker.hasSynced()) {
                            try {
                                Thread.sleep(1000L);
                                LOGGER.info("Failed to sync retrying");
                            } catch (InterruptedException ex) {
                            }
                        }
                        LOGGER.info("All nodes have synced party info");
                        partyInfoSyncLatch.countDown();
                    });

            if (!partyInfoSyncLatch.await(2, TimeUnit.MINUTES)) {
                Description de = Description.createSuiteDescription(getTestClass().getJavaClass());
                notifier.fireTestFailure(new Failure(de, new IllegalStateException("Unable to sync party info nodes")));
            }

            executorService.shutdown();

            super.run(notifier);

            try {
                ExecutionContext.destroyContext();
                setupDatabase.dropAll();
            } finally {
                databaseServer.stop();
            }
        } catch (Throwable ex) {
            Description de = Description.createSuiteDescription(getTestClass().getJavaClass());
            notifier.fireTestFailure(new Failure(de, ex));
        } finally {
            executors.forEach(ExecManager::stop);
        }
    }
}
