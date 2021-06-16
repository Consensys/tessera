package exec;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.test.DBType;
import config.ConfigDescriptor;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suite.EnclaveType;
import suite.ExecutionContext;
import suite.ServerStatusCheck;
import suite.ServerStatusCheckExecutor;

public class NodeExecManager implements ExecManager {

  private final ExecutorService executorService = Executors.newCachedThreadPool();

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeExecManager.class);

  private final ConfigDescriptor configDescriptor;

  private final Path pid;

  private final String nodeId;

  public NodeExecManager(ConfigDescriptor configDescriptor) {
    this.configDescriptor = configDescriptor;
    this.pid =
        Paths.get(
            System.getProperty("java.io.tmpdir"),
            "node-" + configDescriptor.getAlias().name() + ".pid");
    this.nodeId =
        suite.NodeId.generate(ExecutionContext.currentContext(), configDescriptor.getAlias());
  }

  private final URL logbackConfigFile = NodeExecManager.class.getResource("/logback-node.xml");

  @Override
  public Process doStart() throws Exception {
    ExecutionContext executionContext = ExecutionContext.currentContext();

    Path startScript;
    if (EncryptorType.CUSTOM.equals(executionContext.getEncryptorType())) {
      startScript = Paths.get(System.getProperty("application.kalium.jar"));
    } else {
      startScript = Paths.get(System.getProperty("application.jar"));
    }

    ExecArgsBuilder argsBuilder =
        new ExecArgsBuilder()
            .withStartScript(startScript)
            .withPidFile(pid)
            .withConfigFile(configDescriptor.getPath());
    // .withArg("-jdbc.autoCreateTables", "true");

    if (executionContext.getEnclaveType() == EnclaveType.REMOTE) {
      Path enclaveJar =
          Paths.get(
              System.getProperty(
                  "enclave.jaxrs.jar",
                  "../../enclave/enclave-jaxrs/target/enclave-jaxrs-0.10-SNAPSHOT.jar"));
      // argsBuilder.withClassPathItem(enclaveJar);
    }

    if (executionContext.getDbType() == DBType.HSQL) {
      argsBuilder.withJvmArg("-Dhsqldb.reconfig_logging=false");
    }

    if (executionContext.getDbType() != DBType.H2) {
      final Path jdbcJar =
          Paths.get(
              System.getProperty(
                  "jdbc." + executionContext.getDbType().name().toLowerCase() + ".jar"));
      argsBuilder.withClassPathItem(jdbcJar);
    }

    List<String> args = argsBuilder.build();

    LOGGER.info("Exec : {}", String.join(" ", args));

    List<String> javaOptions =
        List.of(
            "-Dnode.number=".concat(nodeId),
            "-Dlogback.configurationFile=" + logbackConfigFile.toString());

    if (System.getProperties().containsKey("jdbc.dir")) {
      //  javaOpts += " -Djava.ext.dirs=" + System.getProperty("jdbc.dir");
    }

    Map<String, String> env = new HashMap<>();
    env.put("JAVA_OPTS", String.join(" ", javaOptions));

    LOGGER.debug("Set env JAVA_OPTS {}", javaOptions);

    final Process process = ExecUtils.start(args, executorService, env);

    List<ServerStatusCheckExecutor> serverStatusCheckList =
        configDescriptor.getConfig().getServerConfigs().stream()
            .filter(s -> s.getApp() != AppType.ENCLAVE)
            .map(ServerStatusCheck::create)
            .map(ServerStatusCheckExecutor::new)
            .collect(Collectors.toList());

    serverStatusCheckList.forEach(
        s -> {
          LOGGER.info("Created {}", s);
        });

    CountDownLatch startUpLatch = new CountDownLatch(serverStatusCheckList.size());

    executorService
        .invokeAll(serverStatusCheckList)
        .forEach(
            f -> {
              try {
                f.get(30, TimeUnit.SECONDS);
                startUpLatch.countDown();
              } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                LOGGER.debug(null, ex);
                LOGGER.error("Exception message {}", ex.getMessage());
              }
            });

    boolean started = startUpLatch.await(2, TimeUnit.MINUTES);

    if (!started) {
      LOGGER.error("Not started {}", pid);
    }

    executorService.submit(
        () -> {
          try {
            int exitCode = process.waitFor();
            LOGGER.info("Node {} exited with code {}", nodeId, exitCode);
          } catch (InterruptedException ex) {
            LOGGER.warn(ex.getMessage());
          }
        });

    return process;
  }

  @Override
  public void doStop() throws Exception {
    LOGGER.info("Stopping Node: {}, Pid: {}", nodeId, pid);
    try {
      ExecUtils.kill(pid);
    } finally {
      executorService.shutdown();
    }
  }

  public ConfigDescriptor getConfigDescriptor() {
    return configDescriptor;
  }
}
