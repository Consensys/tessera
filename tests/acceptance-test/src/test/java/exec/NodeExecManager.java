package exec;

import com.quorum.tessera.Launcher;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.io.FilesDelegate;
import com.quorum.tessera.test.DBType;
import com.quorum.tessera.test.util.ElUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import suite.EnclaveType;
import suite.ExecutionContext;
import suite.PartyInfoChecker;
import suite.ServerStatusCheck;
import suite.ServerStatusCheckExecutor;
import suite.SocketType;
import suite.Utils;

public class NodeExecManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeExecManager.class);

    private final Map<String, Path> pids = new HashMap<>();

    private final Map<String, URL> configFiles;

    private final CommunicationType communicationType;

    private final ExecutionContext executionContext;

    private final DBType dbType;

    private final URL logbackConfigFile = NodeExecManager.class.getResource("/logback-node.xml");

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public NodeExecManager(ExecutionContext executionContext) {
        this.executionContext = executionContext;

        this.communicationType = executionContext.getCommunicationType();
        this.dbType = executionContext.getDbType();

        this.configFiles = executionContext.getConfigs().stream()
                .collect(Collectors.toMap(c -> c.getAlias().name(), c -> Utils.toUrl(c.getPath())));
    }

    private String findJarFilePath(String jar) {
        return Objects.requireNonNull(System.getProperty(jar, null),
                "System property " + jar + " is undefined.");
    }

    public void startNodes() throws Exception {
        List<String> nodeAliases = Arrays.asList(configFiles.keySet().toArray(new String[0]));
        Collections.shuffle(nodeAliases);

        for (String nodeAlias : nodeAliases) {
            LOGGER.info("Starting Node {}", nodeAlias);
            start(nodeAlias);
            LOGGER.info("Started Node {}", nodeAlias);
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);

        LOGGER.info("Creating party info check thread");
        PartyInfoChecker partyInfoChecker = PartyInfoChecker.create(communicationType);

        executorService.submit(() -> {
            
            while (true) {

                LOGGER.info("Check party info");
                if (partyInfoChecker.hasSynced()) {
                    LOGGER.info("Party info propagated");
                    countDownLatch.countDown();
                    break;
                } else {
                    try{
                        LOGGER.info("Party info not synced yet. Sleep for 3 secs");
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException ex) {
                    }
                }

            }
        }).get(3, TimeUnit.MINUTES);

        if (!countDownLatch.await(3, TimeUnit.MINUTES)) {
            throw new RuntimeException("Unable to sync party info");
        }
    }

    public void stopNodes() throws Exception {
        List<String> args = new ArrayList<>();
        args.add("kill");

        pids.values().stream()
                .flatMap(p -> {
                    try{
                        return Files.readAllLines(p).stream();
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                })
                .filter(Objects::nonNull)
                .filter(s -> !Objects.equals("", s))
                .forEach(args::add);

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = processBuilder.start();

        int exitCode = process.waitFor();
    }

    private void start(String nodeAlias) throws Exception {

        final String tesseraJar = findJarFilePath("application.jar");
        final String enclaveJar = findJarFilePath("enclave.jaxrs.jar");

        final URL configFile = configFiles.get(nodeAlias);

        final Config config = JaxbUtil.unmarshal(configFile.openStream(), Config.class);

        final Path pid = Paths.get(System.getProperty("java.io.tmpdir"), "pid" + nodeAlias + ".pid");

        pids.put(nodeAlias, pid);

        String nodeId = String.join("-", communicationType.name().toLowerCase(),
                executionContext.getSocketType().name().toLowerCase(),
                dbType.name().toLowerCase(),
                "enclave",
                executionContext.getEnclaveType().name().toLowerCase(),
                nodeAlias);

        ExecArgsBuilder argsBuilder = new ExecArgsBuilder()
                .withJvmArg("-Ddebug=true")
                .withJvmArg("-Dnode.number=" + nodeId)
                .withMainClass(Launcher.class)
                .withPidFile(pid)
                .withConfigFile(ElUtil.createAndPopulatePaths(configFile))
                .withJvmArg("-Dlogback.configurationFile=" + logbackConfigFile.getFile())
                .withClassPathItem(Paths.get(tesseraJar))
                .withArg("-jdbc.autoCreateTables", "true");

        if (executionContext.getEnclaveType() == EnclaveType.REMOTE) {
            argsBuilder.withClassPathItem(Paths.get(enclaveJar)); 
        }

        if (dbType == DBType.HSQL) {
            argsBuilder.withJvmArg("-Dhsqldb.reconfig_logging=false");
        }

        if (dbType != DBType.H2) {
            final String jdbcJar = findJarFilePath("jdbc." + dbType.name().toLowerCase() + ".jar");
            argsBuilder.withClassPathItem(Paths.get(jdbcJar));
        }
        List<String> args = argsBuilder.build();

        LOGGER.info("Exec : {}", String.join(" ", args));

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        executorService.submit(() -> {

            try (BufferedReader reader = Stream.of(process.getInputStream())
                    .map(InputStreamReader::new)
                    .map(BufferedReader::new)
                    .findAny().get()){

                String line = null;
                while ((line = reader.readLine()) != null) {
                    LOGGER.info("Exec line {} : {}", nodeAlias, line);
                }

            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });

        List<ServerStatusCheckExecutor> serverStatusCheckList = config.getServerConfigs().stream()
                .filter(s -> s.getApp() != AppType.ENCLAVE)
                .map(ServerStatusCheck::create)
                .map(ServerStatusCheckExecutor::new)
                .collect(Collectors.toList());

        serverStatusCheckList.forEach(s -> {
            LOGGER.info("Created {}", s);

        });

        CountDownLatch startUpLatch = new CountDownLatch(serverStatusCheckList.size());

        executorService.invokeAll(serverStatusCheckList).forEach(f -> {
            try{
                f.get(30, TimeUnit.SECONDS);
                startUpLatch.countDown();
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                LOGGER.debug(null, ex);
                LOGGER.error("Exception message {}", ex.getMessage());
            }
        });

        boolean started = startUpLatch.await(2, TimeUnit.MINUTES);

        if (!started) {
            LOGGER.error("Not started {}", communicationType);
        }

        executorService.submit(() -> {
            try{
                int exitCode = process.waitFor();
                LOGGER.info("Node {} exited with code {}", nodeId, exitCode);
            } catch (InterruptedException ex) {
                LOGGER.warn(ex.getMessage());
            }
        });

    }

    private void kill(String nodeAlias) throws Exception {

        FilesDelegate fileDelegate = FilesDelegate.create();
        Path pidFile = pids.remove(nodeAlias);
        fileDelegate.lines(pidFile);

        String pid = Files.lines(pidFile).findFirst().get();
        ExecUtils.kill(pid);

    }
    

    

    public static void main(String[] args) throws Exception {
        System.setProperty("application.jar", "/home/nicolae/Develop/java/IJWorkspaces/tessera/tessera-app/target/tessera-app-0.9-SNAPSHOT-app.jar");
        System.setProperty("jdbc.sqlite.jar", "/home/nicolae/.m2/repository/org/xerial/sqlite-jdbc/3.23.1/sqlite-jdbc-3.23.1.jar");
        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        NodeExecManager pm = new NodeExecManager(ExecutionContext.Builder.create()
                .with(CommunicationType.REST)
                .with(DBType.H2)
                .with(SocketType.HTTP)
                .build());
        
        pm.startNodes();

        System.in.read();

        pm.stopNodes();

    }

}
