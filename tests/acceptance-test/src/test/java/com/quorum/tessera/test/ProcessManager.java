package com.quorum.tessera.test;

import com.google.protobuf.Empty;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.grpc.p2p.TesseraGrpc;
import com.quorum.tessera.grpc.p2p.UpCheckMessage;
import com.quorum.tessera.io.FilesDelegate;
import com.quorum.tessera.test.util.ElUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ProcessManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessManager.class);

    private final Map<String, Path> pids = new HashMap<>();

    private final Map<String, URL> configFiles;

    private final CommunicationType communicationType;

    private final URL logbackConfigFile = ProcessManager.class.getResource("/logback-node.xml");

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ProcessManager(CommunicationType communicationType) {
        this.communicationType = Objects.requireNonNull(communicationType);

        String pathTemplate = "/" + communicationType.name().toLowerCase() + "/config%s.json";
        final Map<String, URL> configs = new HashMap<>();
        configs.put("A", getClass().getResource(String.format(pathTemplate, "1")));
        configs.put("B", getClass().getResource(String.format(pathTemplate, "2")));
        configs.put("C", getClass().getResource(String.format(pathTemplate, "3")));
        configs.put("D", getClass().getResource(String.format(pathTemplate, "4")));
        configs.put("E", getClass().getResource(String.format(pathTemplate, "-whitelist")));
        this.configFiles = Collections.unmodifiableMap(configs);
    }

    public String findJarFilePath() {
        return Objects.requireNonNull(System.getProperty("application.jar", null),
                "System property application.jar is undefined.");
    }

    public void startNodes() throws Exception {
        List<String> nodeAliases = Arrays.asList(configFiles.keySet().toArray(new String[0]));
        Collections.shuffle(nodeAliases);

        for (String nodeAlias : nodeAliases) {
            start(nodeAlias);
        }
        // sleep a little before starting the tests (give the party info a chance to propagate)
        LOGGER.info("sleeping a little before allowing the tests to start (making sure the party info propagates in the cluster)");
        Thread.sleep(5000);
    }

    public void stopNodes() throws Exception {
        List<String> args = new ArrayList<>();
        args.add("kill");

        pids.values().stream()
                .flatMap(p -> {
                    try {
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

    public void start(String nodeAlias) throws Exception {
        final String jarfile = findJarFilePath();

        URL configFile = configFiles.get(nodeAlias);
        Path pid = Paths.get(System.getProperty("java.io.tmpdir"), "pid" + nodeAlias + ".pid");

        pids.put(nodeAlias, pid);

        List<String> args = Arrays.asList(
                "java",
                "-Dspring.profiles.active=disable-unixsocket,disable-sync-poller",
                "-Dnode.number=" + nodeAlias,
                "-Dlogback.configurationFile=" + logbackConfigFile.getFile(),
                "-Ddebug=true",
                "-jar",
                jarfile,
                "-configfile",
                ElUtil.createAndPopulatePaths(configFile).toAbsolutePath().toString(),
                "-pidfile",
                pid.toAbsolutePath().toString(),
                "-jdbc.autoCreateTables", "true"
        );
        System.out.println(String.join(" ", args));

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        executorService.submit(() -> {

            try(BufferedReader reader = Stream.of(process.getInputStream())
                    .map(InputStreamReader::new)
                    .map(BufferedReader::new)
                    .findAny().get()) {

                String line = null;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });

        final String nodeId = nodeAlias;

        final Config config = JaxbUtil.unmarshal(configFile.openStream(), Config.class);

        final URL bindingUrl = UriBuilder.fromUri(config.getP2PServerConfig().getBindingUri()).path("upcheck").build().toURL();

        CountDownLatch startUpLatch = new CountDownLatch(1);

        if (communicationType == CommunicationType.GRPC) {
            URL grpcUrl = UriBuilder.fromUri(config.getP2PServerConfig().getBindingUri())
                    .path("upcheck")
                    .build().toURL();

            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(grpcUrl.getHost(), grpcUrl.getPort())
                    .usePlaintext()
                    .build();

            executorService.submit(() -> {

                while (true) {
                    try {

                        UpCheckMessage result = TesseraGrpc.newBlockingStub(channel).getUpCheck(Empty.getDefaultInstance());

                        System.out.println(grpcUrl + " started. " + result.getUpCheck());

                        startUpLatch.countDown();
                        return;
                    } catch (Exception ex) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(200L);
                        } catch (InterruptedException ex1) {
                        }
                    }
                }

            });
        } else {

            executorService.submit(() -> {

                while (true) {
                    try {
                        HttpURLConnection conn = (HttpURLConnection) bindingUrl.openConnection();
                        conn.connect();

                        System.out.println(bindingUrl + " started." + conn.getResponseCode());

                        startUpLatch.countDown();
                        return;
                    } catch (IOException ex) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(200L);
                        } catch (InterruptedException ex1) {
                        }
                    }
                }

            });
        }

        boolean started = startUpLatch.await(30, TimeUnit.SECONDS);

        if (!started) {
            System.err.println(bindingUrl + " Not started. ");
        }

        executorService.submit(() -> {
            try {
                int exitCode = process.waitFor();
                if (0 != exitCode) {
                    System.err.println("Node " + nodeId + " exited with code " + exitCode);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });

        startUpLatch.await(30, TimeUnit.SECONDS);

    }

    public void kill(String nodeAlias) throws Exception {

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
        System.setProperty("application.jar", "/Users/mark/Projects/tessera/tessera-app/target/tessera-app-0.8-SNAPSHOT-app.jar");

        ProcessManager pm = new ProcessManager(CommunicationType.REST);
        pm.startNodes();

        System.in.read();

        pm.stopNodes();

    }

}
