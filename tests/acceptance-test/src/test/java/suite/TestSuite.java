package suite;

import db.DatabaseServer;
import db.SetupDatabase;
import exec.EnclaveExecManager;
import exec.ExecManager;
import exec.NodeExecManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class TestSuite extends Suite {

  public TestSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
    super(klass, builder);
  }

  @Override
  public void run(RunNotifier notifier) {

    final List<ExecManager> executors = new ArrayList<>();
    try {

      final ProcessConfig annotatedConfig =
          Arrays.stream(getRunnerAnnotations())
              .filter(ProcessConfig.class::isInstance)
              .map(ProcessConfig.class::cast)
              .findAny()
              .orElseThrow(() -> new AssertionError("No Test config found"));

      ExecutionContext executionContext =
          ExecutionContext.Builder.create()
              .with(annotatedConfig.communicationType())
              .with(annotatedConfig.dbType())
              .with(annotatedConfig.socketType())
              .with(annotatedConfig.enclaveType())
              .withAdmin(annotatedConfig.admin())
              .with(annotatedConfig.encryptorType())
              .with(annotatedConfig.clientMode())
              .prefix(annotatedConfig.prefix())
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

      PartyInfoChecker partyInfoChecker =
          PartyInfoChecker.create(executionContext.getCommunicationType());

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

      if (!partyInfoSyncLatch.await(10, TimeUnit.MINUTES)) {

        Description de = getDescription();
        notifier.fireTestFailure(
            new Failure(de, new IllegalStateException("Unable to sync party info nodes")));
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
      Description de = getDescription();
      notifier.fireTestFailure(new Failure(de, ex));
    } finally {
      executors.forEach(ExecManager::stop);
    }
  }

  //    @Override
  //    public Description getDescription() {
  //        String s = NodeId.generate(ExecutionContext.currentContext());
  //
  //        ExecutionContext executionContext = ExecutionContext.currentContext();
  //
  //        return Description.createTestDescription(getTestClass().getJavaClass(),s);
  //    }
}
