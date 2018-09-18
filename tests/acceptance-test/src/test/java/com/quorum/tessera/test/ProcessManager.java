package com.quorum.tessera.test;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.grpc.GrpcSuite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ProcessManager {

    private final List<Path> pids = new ArrayList<>();

    private final CommunicationType communicationType;

    public ProcessManager(CommunicationType communicationType) {
        this.communicationType = communicationType;
    }

    public void startNodes() throws Exception {

        final String jarfile = Objects.requireNonNull(System.getProperty("application.jar", null), "System property application.jar is undefined.");

        URL logbackConfigFile = GrpcSuite.class.getResource("/logback-node.xml");

        ExecutorService executorService = Executors.newCachedThreadPool();

        for (int nodeNumber = 1; nodeNumber <= 4; nodeNumber++) {

            Path configFile = com.quorum.tessera.test.util.ElUtil.createAndPopulatePaths(GrpcSuite.class.getResource("/" + communicationType.name().toLowerCase() + "/config" + nodeNumber + ".json"));

            Path pid = Paths.get(System.getProperty("java.io.tmpdir"), "pid" + nodeNumber + ".pid");

            pids.add(pid);

            List<String> args = Arrays.asList(
                    "java",
                    "-Dspring.profiles.active=disable-unixsocket",
                    "-Dnode.number=" + nodeNumber,
                    "-Dlogback.configurationFile=" + logbackConfigFile.getFile(),
                    "-jar",
                    jarfile,
                    "-configfile",
                    configFile.toAbsolutePath().toString(),
                    "-pidfile",
                    pid.toAbsolutePath().toString(),
                    "-server.communicationType",
                    communicationType.name()
            );

            System.out.println(String.join(" ", args));

            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            executorService.submit(() -> {

                try (BufferedReader reader = Stream.of(process.getInputStream())
                        .map(InputStreamReader::new)
                        .map(BufferedReader::new).findAny().get()) {

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            CountDownLatch startUpLatch = new CountDownLatch(1);
            final int nodeId = nodeNumber;
            executorService.submit(() -> {
                try {
                    int exitCode = process.waitFor();
                    if (0 != exitCode) {
                        System.err.println("Node " + nodeId + " exited with code " + exitCode);
                    }
                    startUpLatch.countDown();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });

            startUpLatch.await(30, TimeUnit.SECONDS);

        }
    }

    public void stopNodes() throws Exception {
        List<String> args = new ArrayList<>();
        args.add("kill");

        pids.stream()
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

    public static void main(String[] args) throws Exception {
        System.setProperty("application.jar", "/Users/mark/Library/Maven/repo/com/quorum/tessera/tessera-app/0.7-SNAPSHOT/tessera-app-0.7-SNAPSHOT-app.jar");

        ProcessManager pm = new ProcessManager(CommunicationType.REST);
        pm.startNodes();

        System.in.read();

        pm.stopNodes();

    }

}
