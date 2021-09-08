package exec;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.JaxbUtil;
import config.ConfigDescriptor;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suite.ExecutionContext;
import suite.ServerStatusCheck;
import suite.ServerStatusCheckExecutor;

public class EnclaveExecManager implements ExecManager {

  private final ExecutorService executorService = Executors.newCachedThreadPool();

  private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveExecManager.class);

  private final ConfigDescriptor configDescriptor;

  private final Path pid;

  private final String nodeId;

  public EnclaveExecManager(ConfigDescriptor configDescriptor) {
    this.configDescriptor = configDescriptor;
    this.pid =
        Paths.get(
            System.getProperty("java.io.tmpdir"),
            "enclave" + configDescriptor.getAlias().name() + ".pid");
    this.nodeId =
        suite.NodeId.generate(ExecutionContext.currentContext(), configDescriptor.getAlias());
  }

  private final URL logbackConfigFile =
      EnclaveExecManager.class.getResource("/logback-enclave.xml");

  @Override
  public Process doStart() throws Exception {

    Path startScript;
    if (EncryptorType.CUSTOM.equals(configDescriptor.getConfig().getEncryptor().getType())) {
      startScript = Paths.get(System.getProperty("enclave.jaxrs.server.kalium.jar"));
    } else {
      startScript = Paths.get(System.getProperty("enclave.jaxrs.server.jar"));
    }

    final ServerConfig serverConfig =
        configDescriptor.getEnclaveConfig().get().getServerConfigs().get(0);

    ExecArgsBuilder execArgsBuilder = new ExecArgsBuilder();

    final List<String> cmd =
        execArgsBuilder
            .withPidFile(pid)
            .withJvmArg("-Dnode.number=" + nodeId)
            .withJvmArg("-Dlogback.configurationFile=" + logbackConfigFile)
            .withStartScript(startScript)
            .withConfigFile(configDescriptor.getEnclavePath())
            .build();

    LOGGER.info("Starting enclave {}", configDescriptor.getAlias());

    String javaOpts =
        "-Dnode.number="
            .concat(nodeId)
            .concat(" ")
            .concat("-Dlogback.configurationFile=")
            .concat(logbackConfigFile.toString());

    Map<String, String> env = new HashMap<>();
    env.put("JAVA_OPTS", javaOpts);

    LOGGER.info("Set env JAVA_OPTS {}", javaOpts);

    Process process = ExecUtils.start(cmd, executorService, env);

    ServerStatusCheckExecutor serverStatusCheckExecutor =
        new ServerStatusCheckExecutor(ServerStatusCheck.create(serverConfig));

    Future<Boolean> future = executorService.submit(serverStatusCheckExecutor);

    Boolean result = future.get(3, TimeUnit.MINUTES);

    if (!result) {
      throw new IllegalStateException("Enclave server not started");
    }

    LOGGER.info("Started enclave {}", configDescriptor.getAlias());

    return process;
  }

  @Override
  public void doStop() throws Exception {
    LOGGER.info("Stopping Enclave : {}, Pid: {}", nodeId, pid);
    try {
      ExecUtils.kill(pid);
    } finally {
      executorService.shutdown();
    }
  }

  public static void main(String[] args) throws Exception {

    System.setProperty(
        "jakarta.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
    System.setProperty(
        "jakarta.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

    final Config enclaveConfig = new Config();

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setApp(AppType.ENCLAVE);
    serverConfig.setBindingAddress("http://0.0.0.0:8989");
    serverConfig.setServerAddress("http://localhost:8989");
    serverConfig.setCommunicationType(CommunicationType.REST);

    enclaveConfig.setServerConfigs(Arrays.asList(serverConfig));

    enclaveConfig.setKeys(new KeyConfiguration());
    KeyData keyPairData = new KeyData();
    keyPairData.setPublicKey("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    keyPairData.setPrivateKey("yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");

    enclaveConfig.getKeys().setKeyData(Arrays.asList(keyPairData));

    Path configFile = Paths.get("target", UUID.randomUUID().toString() + ".json");

    try (OutputStream out = Files.newOutputStream(configFile)) {
      JaxbUtil.marshalWithNoValidation(enclaveConfig, out);
    }

    // EnclaveExecManager enclaveExecManager = new EnclaveExecManager(configFile);
    // enclaveExecManager.start(serverConfig);
  }

  @Override
  public ConfigDescriptor getConfigDescriptor() {
    return configDescriptor;
  }
}
