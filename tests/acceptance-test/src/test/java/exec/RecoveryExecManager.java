package exec;

import config.ConfigDescriptor;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suite.ExecutionContext;

public class RecoveryExecManager implements ExecManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryExecManager.class);

  private final ExecutorService executorService = Executors.newCachedThreadPool();

  private final URL logbackConfigFile = RecoveryExecManager.class.getResource("/logback-node.xml");

  private ConfigDescriptor configDescriptor;

  private final Path pid;

  private final String nodeId;

  public RecoveryExecManager(ConfigDescriptor configDescriptor) {
    this.configDescriptor = configDescriptor;
    this.pid =
        Paths.get(
            System.getProperty("java.io.tmpdir"),
            "recoverynode-" + configDescriptor.getAlias().name() + ".pid");
    this.nodeId =
        suite.NodeId.generate(ExecutionContext.currentContext(), configDescriptor.getAlias());
  }

  @Override
  public Process doStart() throws Exception {

    Path nodeServerJar = Paths.get(System.getProperty("application.jar"));

    ExecutionContext executionContext = ExecutionContext.currentContext();

    ExecArgsBuilder argsBuilder =
        new ExecArgsBuilder()
            .withArg("--recover")
            .withStartScript(nodeServerJar)
            .withPidFile(pid)
            .withConfigFile(configDescriptor.getPath());

    List<String> args = argsBuilder.build();

    List<String> jvmArgs =
        List.of(
            "-Dlogback.configurationFile=" + logbackConfigFile.getFile(),
            "-Dnode.number=" + nodeId.concat("-").concat("recover"));

    Map<String, String> env = new HashMap<>();
    env.put("JAVA_OPTS", String.join(" ", jvmArgs));
    final Process process = ExecUtils.start(args, executorService, env);

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

  @Override
  public ConfigDescriptor getConfigDescriptor() {
    return configDescriptor;
  }
}
