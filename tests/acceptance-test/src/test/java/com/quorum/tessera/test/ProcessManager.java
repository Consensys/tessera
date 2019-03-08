package com.quorum.tessera.test;

import com.quorum.tessera.Launcher;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.io.FilesDelegate;
import com.quorum.tessera.test.util.ElUtil;
import exec.ExecArgsBuilder;
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
import suite.ExecutionContext;
import suite.PartyInfoChecker;
import suite.ServerStatusCheck;
import suite.ServerStatusCheckExecutor;
import suite.SocketType;

public class ProcessManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessManager.class);

    private final Map<String, Path> pids = new HashMap<>();

    private final Map<String, URL> configFiles;

    private final CommunicationType communicationType;

    private final DBType dbType;

    private final URL logbackConfigFile = ProcessManager.class.getResource("/logback-node.xml");

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ProcessManager(ExecutionContext executionContext) {
        this(executionContext.getCommunicationType(), executionContext.getDbType(), executionContext.getSocketType());
    }

    private ProcessManager(CommunicationType communicationType, DBType dbType, SocketType socketType) {
        this.communicationType = Objects.requireNonNull(communicationType);
        this.dbType = Objects.requireNonNull(dbType);

        String pathTemplate = "/" + communicationType.name().toLowerCase() + "/" + socketType.name().toLowerCase()
                + "/" + dbType.name().toLowerCase() + "/config%s.json";

        final Map<String, URL> configs = new HashMap<>();
        configs.put("A", getClass().getResource(String.format(pathTemplate, "1")));
        configs.put("B", getClass().getResource(String.format(pathTemplate, "2")));
        configs.put("C", getClass().getResource(String.format(pathTemplate, "3")));
        configs.put("D", getClass().getResource(String.format(pathTemplate, "4")));
        configs.put("E", getClass().getResource(String.format(pathTemplate, "-whitelist")));
        this.configFiles = Collections.unmodifiableMap(configs);
    }

    private String findJarFilePath(String jar) {
        return Objects.requireNonNull(System.getProperty(jar, null),
                "System property " + jar + " is undefined.");
    }

    public void startNodes() throws Exception {
        List<String> nodeAliases = Arrays.asList(configFiles.keySet().toArray(new String[0]));
        Collections.shuffle(nodeAliases);

        for (String nodeAlias : nodeAliases) {
            LOGGER.info("Starting {}", nodeAlias);
            start(nodeAlias);
            LOGGER.info("Started {}", nodeAlias);
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);

        LOGGER.info("Creating party info check thread");
        PartyInfoChecker partyInfoChecker = PartyInfoChecker.create(communicationType);
        
        executorService.submit(() -> {

            while (true) {
               
                LOGGER.info("Check party info");
                if (partyInfoChecker.hasSynced()) {
                    countDownLatch.countDown();
                    break;
                }
                try{
                    LOGGER.info("Party info not synced yet. Sleep for 3 secs");
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException ex) {
                }

            }
        }).get(3, TimeUnit.MINUTES);

        countDownLatch.await(3, TimeUnit.MINUTES);
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

        ExecArgsBuilder argsBuilder = new ExecArgsBuilder()
                .withJvmArg("-Ddebug=true")
                .withJvmArg("-Dnode.number=" + nodeAlias)
                .withMainClass(Launcher.class)
                .withPidFile(pid)
                .withConfigFile(ElUtil.createAndPopulatePaths(configFile))
                .withJvmArg("-Dlogback.configurationFile=" + logbackConfigFile.getFile())
                .withClassPathItem(Paths.get(tesseraJar))
                //  .withClassPathItem(Paths.get(enclaveJar))
                .withArg("-jdbc.autoCreateTables", "true");

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
                    LOGGER.info("Exec line : {}", line);
                }

            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });

        final String nodeId = nodeAlias;

        List<ServerStatusCheckExecutor> serverStatusCheckList = config.getServerConfigs().stream()
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
               LOGGER.debug(null,ex);
               LOGGER.error("Exception message {}",ex.getMessage());
            }
        });

        boolean started = startUpLatch.await(2, TimeUnit.MINUTES);

        if (!started) {
            LOGGER.error("Not started {}",communicationType);
        }

        executorService.submit(() -> {
            try{
                int exitCode = process.waitFor();
                if (0 != exitCode) {
                     LOGGER.error("Node {} exited with code {}",nodeId, exitCode);
                }
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

        List<String> args = Arrays.asList("kill", pid);
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = processBuilder.start();

        int exitCode = process.waitFor();
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("application.jar", "/home/nicolae/Develop/java/IJWorkspaces/tessera/tessera-app/target/tessera-app-0.9-SNAPSHOT-app.jar");
        System.setProperty("jdbc.sqlite.jar", "/home/nicolae/.m2/repository/org/xerial/sqlite-jdbc/3.23.1/sqlite-jdbc-3.23.1.jar");
        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        ProcessManager pm = new ProcessManager(CommunicationType.REST, DBType.SQLITE, SocketType.HTTP);
        pm.startNodes();

        System.in.read();

        pm.stopNodes();

    }

}
