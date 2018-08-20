package com.quorum.tessera.test.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class KeyGenIT {

    private static final Path buildDir = Paths.get("target/");

    private ExecutorService executorService;

    private final KeyGenTestConfig testConfig;

    private final List<String> argList;

    public KeyGenIT(KeyGenTestConfig testConfig) throws IOException {
        this.testConfig = testConfig;

        this.argList = new ArrayList<>();
        this.argList.add("java");
        this.argList.add("-jar");
        this.argList.add(testConfig.getApplicationJarPath().toString());
        this.argList.add("-keygen");
        this.argList.addAll(testConfig.getArgs());
    }

    @Before
    public void onSetup() throws IOException {

        executorService = Executors.newFixedThreadPool(2);
        Files.deleteIfExists(testConfig.getExpectedKeyPath());
        Files.deleteIfExists(testConfig.getExpectedPubKeyPath());
    }

    @After
    public void onTearDown() throws IOException {

        executorService.shutdown();

        Files.deleteIfExists(testConfig.getExpectedKeyPath());
        Files.deleteIfExists(testConfig.getExpectedPubKeyPath());
    }

    @Test
    public void generateKeys() throws Exception {

        ProcessBuilder processBuilder = new ProcessBuilder(argList);
        processBuilder.directory(buildDir.toFile());

        Process process = processBuilder.start();
        executorService.submit(new StreamConsumer(process.getInputStream(), process.getOutputStream()));
        executorService.submit(new StreamConsumer(process.getErrorStream(), process.getOutputStream()));

        int result = process.waitFor();
        
        assertThat(result).isEqualTo(testConfig.getExpectedExitCode());
        assertThat(testConfig.getExpectedKeyPath()).exists();
        assertThat(testConfig.getExpectedPubKeyPath()).exists();

    }

    @Parameterized.Parameters(name = "tessera -keygen '{index}'")
    public static List<KeyGenTestConfig> parameters() {
        
        String appPath  = System.getProperty("application.jar");
        if(Objects.equals("", appPath)) {
            throw new IllegalStateException("No application.jar system property defined. ");
        }
        Path applicationJarPath = Paths.get(appPath);
        
        KeyGenTestConfig minimal = new KeyGenTestConfig();
        minimal.setExpectedKeyPath(buildDir.resolve(".key"));
        minimal.setExpectedPubKeyPath(buildDir.resolve(".pub"));
        minimal.setDescription("Keygen with no args creates default keys");
        minimal.setApplicationJarPath(applicationJarPath);

        
        return Arrays.asList(minimal);
    }

    static class StreamConsumer implements Runnable {

        private final InputStream inputStream;

        private final OutputStream outputStream;

        StreamConsumer(InputStream inputStream, OutputStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {

            try (BufferedReader reader = Stream.of(inputStream)
                    .map(InputStreamReader::new)
                    .map(BufferedReader::new).findAny().get()) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if ("Enter a password if you want to lock the private key or leave blank".equals(line)
                            || "Please re-enter the password (or lack of) to confirm".equals(line)) {
                        outputStream.write(System.lineSeparator().getBytes());
                        outputStream.flush();
                    }
                    System.out.println("HERE " + line);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

    }

    static class KeyGenTestConfig {

        private String description;

        private final List<String> args = new ArrayList<>();

        private Path expectedKeyPath;

        private Path expectedPubKeyPath;

        private Path applicationJarPath;
        
        private Integer expectedExitCode = 0;
        
        public List<String> getArgs() {
            return args;
        }

        public void add(String... args) {
            this.args.addAll(Arrays.asList(args));
        }

        public Path getExpectedKeyPath() {
            return expectedKeyPath;
        }

        public void setExpectedKeyPath(Path expectedKeyPath) {
            this.expectedKeyPath = expectedKeyPath;
        }

        public Path getExpectedPubKeyPath() {
            return expectedPubKeyPath;
        }

        public void setExpectedPubKeyPath(Path expectedPubKeyPath) {
            this.expectedPubKeyPath = expectedPubKeyPath;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Path getApplicationJarPath() {
            return applicationJarPath;
        }

        public void setApplicationJarPath(Path applicationJarPath) {
            this.applicationJarPath = applicationJarPath;
        }

        public Integer getExpectedExitCode() {
            return expectedExitCode;
        }

        public void setExpectedExitCode(Integer expectedExitCode) {
            this.expectedExitCode = expectedExitCode;
        }
        
        

    }

}
