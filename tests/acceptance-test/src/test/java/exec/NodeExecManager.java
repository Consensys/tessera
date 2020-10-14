package exec;

import com.quorum.tessera.launcher.Main;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.test.DBType;
import config.ConfigDescriptor;

import java.net.URL;
import java.nio.file.Files;
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
            Paths.get(System.getProperty("java.io.tmpdir"), "node-" + configDescriptor.getAlias().name() + ".pid");
        this.nodeId = suite.NodeId.generate(ExecutionContext.currentContext(), configDescriptor.getAlias());
    }

    private final URL logbackConfigFile = NodeExecManager.class.getResource("/logback-node.xml");

    @Override
    public Process doStart() throws Exception {

        Path nodeServerJar =
            Paths.get(
                System.getProperty(
                    "application.jar", "../../tessera-app/target/tessrea-app-0.10-SNAPSHOT-app.jar"));

        ExecutionContext executionContext = ExecutionContext.currentContext();

        ExecArgsBuilder argsBuilder =
            new ExecArgsBuilder()
                .withJvmArg("-Ddebug=true")
                .withJvmArg("-Dnode.number=" + nodeId)
                .withStartScriptOrJarFile(nodeServerJar)
                .withMainClass(Main.class)
                .withPidFile(pid)
                .withConfigFile(configDescriptor.getPath())
                .withJvmArg("-Dlogback.configurationFile=" + logbackConfigFile.getFile())
                .withClassPathItem(nodeServerJar);
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
                Paths.get(System.getProperty("jdbc." + executionContext.getDbType().name().toLowerCase() + ".jar"));
            argsBuilder.withClassPathItem(jdbcJar);
        }

        List<String> args = argsBuilder.build();

        LOGGER.info("Exec : {}", String.join(" ", args));

        String javaOpts =
            "-Dnode.number="
                .concat(nodeId)
                .concat(" ")
                .concat("-Dlogback.configurationFile=")
                .concat(logbackConfigFile.toString());

        LOGGER.info("EXT DIR : {}", System.getProperty("jdbc.dir"));
        if (System.getProperties().containsKey("jdbc.dir")) {
            //  javaOpts += " -Djava.ext.dirs=" + System.getProperty("jdbc.dir");
        }

        Map<String, String> env = new HashMap<>();
        env.put("JAVA_OPTS", javaOpts);

        LOGGER.debug("Set env JAVA_OPTS {}", javaOpts);

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

        String p = Files.lines(pid).findFirst().orElse(null);
        if (p == null) {
            return;
        }
        LOGGER.info("Stopping Node: {}, Pid: {}", nodeId, p);
        try {
            ExecUtils.kill(p);
        } finally {
            executorService.shutdown();
        }
    }

    public ConfigDescriptor getConfigDescriptor() {
        return configDescriptor;
    }
}
