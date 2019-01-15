package com.quorum.tessera.test.vault.hashicorp;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.HashicorpKeyVaultConfig;
import com.quorum.tessera.config.keypairs.HashicorpVaultKeyPair;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.ProcessManager;
import com.quorum.tessera.test.util.ElUtil;
import cucumber.api.java8.En;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.core.UriBuilder;
import java.io.*;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class HashicorpStepDefs implements En {

    private static final Logger LOGGER = LoggerFactory.getLogger(HashicorpStepDefs.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private String vaultToken;

    private String unsealKey;

    private String defaultTrustStore;

    private String defaultKeyStore;

    public HashicorpStepDefs() {
        final AtomicReference<Process> vaultServerProcess = new AtomicReference<>();
        final AtomicReference<Process> tesseraProcess = new AtomicReference<>();

        Before(() -> {
            //only needed when running outside of maven build process
//            System.setProperty("application.jar", "/Users/chris/jpmc-tessera/tessera-app/target/tessera-app-0.8-SNAPSHOT-app.jar");
        });

        Given("the dev vault server has been started", () -> {

            ProcessBuilder vaultServerProcessBuilder = new ProcessBuilder("vault", "server", "-dev");

            vaultServerProcess.set(
                vaultServerProcessBuilder.redirectErrorStream(true)
                                          .start()
            );

            AtomicBoolean isAddressAlreadyInUse = new AtomicBoolean(false);

            executorService.submit(() -> {
                try(BufferedReader reader = Stream.of(vaultServerProcess.get().getInputStream())
                                                  .map(InputStreamReader::new)
                                                  .map(BufferedReader::new)
                                                  .findAny().get()) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        if(line.matches("^Error.+address already in use")) {
                            isAddressAlreadyInUse.set(true);
                        }

                        if(line.matches("^Root Token: .+$")) {
                            String[] components = line.split(" ");
                            vaultToken = components[components.length-1].trim();
                        }
                    }

                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            // wait so that assertion is not evaluated before output is checked
            CountDownLatch startUpLatch = new CountDownLatch(1);
            startUpLatch.await(5, TimeUnit.SECONDS);

            assertThat(isAddressAlreadyInUse).isFalse();

        });

        Given("^the vault server has been started with TLS-enabled$", () -> {
            Path vaultDir = Files.createTempDirectory("vault");
            vaultDir.toFile().deleteOnExit();

            Map<String, Object> params = new HashMap<>();
            params.put("vaultPath", vaultDir.toString());

            Path configFile = ElUtil.createTempFileFromTemplate(getClass().getResource("/vault/tls-config.hcl"), params);

            List<String> args = Arrays.asList(
                "vault",
                "server",
                "-config=" + configFile.toString()
            );
            System.out.println(String.join(" ", args));

            ProcessBuilder vaultServerProcessBuilder = new ProcessBuilder(args);

            vaultServerProcess.set(
                vaultServerProcessBuilder.redirectErrorStream(true)
                    .start()
            );

            AtomicBoolean isAddressAlreadyInUse = new AtomicBoolean(false);

            executorService.submit(() -> {
                try(BufferedReader reader = Stream.of(vaultServerProcess.get().getInputStream())
                    .map(InputStreamReader::new)
                    .map(BufferedReader::new)
                    .findAny().get()) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        if(line.matches("^Error.+address already in use")) {
                            isAddressAlreadyInUse.set(true);
                        }

                        //The root token is not included in the startup logs when using a non-dev server
//                        if(line.matches("^Root Token: .+$")) {
//                            String[] components = line.split(" ");
//                            vaultToken = components[components.length-1].trim();
//                        }
                    }

                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            // wait so that assertion is not evaluated before output is checked
            CountDownLatch startUpLatch = new CountDownLatch(1);
            startUpLatch.await(5, TimeUnit.SECONDS);

            assertThat(isAddressAlreadyInUse).isFalse();

            System.setProperty("javax.net.ssl.keyStoreType", "jks");
            System.setProperty("javax.net.ssl.keyStore", "/Users/chrishounsom/Desktop/san2keystore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "password");
            System.setProperty("javax.net.ssl.trustStoreType", "jks");
            System.setProperty("javax.net.ssl.trustStore", "/Users/chrishounsom/Desktop/san2truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "password");

            //Initialise the vault
            final URL initUrl = UriBuilder.fromUri("https://localhost:8200").path("v1/sys/init").build().toURL();
            HttpsURLConnection initUrlConnection = (HttpsURLConnection) initUrl.openConnection();

            initUrlConnection.setDoOutput(true);
            initUrlConnection.setRequestMethod("PUT");

            String initData = "{\"secret_shares\": 1, \"secret_threshold\": 1}";

            try(OutputStreamWriter writer = new OutputStreamWriter(initUrlConnection.getOutputStream())) {
                writer.write(initData);
            }

            initUrlConnection.connect();
            assertThat(initUrlConnection.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_OK);

            JsonReader initResponseReader = Json.createReader(initUrlConnection.getInputStream());

            JsonObject initResponse = initResponseReader.readObject();

            assertThat(initResponse.getString("root_token")).isNotEmpty();
            vaultToken = initResponse.getString("root_token");

            assertThat(initResponse.getJsonArray("keys_base64").size()).isEqualTo(1);
            assertThat(initResponse.getJsonArray("keys_base64").get(0).toString()).isNotEmpty();
            String quotedUnsealKey = initResponse.getJsonArray("keys_base64").get(0).toString();

            if('\"' == quotedUnsealKey.charAt(0) && '\"' == quotedUnsealKey.charAt(quotedUnsealKey.length() - 1)) {
                unsealKey = quotedUnsealKey.substring(1, quotedUnsealKey.length() - 1);
            }

            //Unseal the vault
            final URL unsealUrl = UriBuilder.fromUri("https://localhost:8200").path("v1/sys/unseal").build().toURL();
            HttpsURLConnection unsealUrlConnection = (HttpsURLConnection) unsealUrl.openConnection();

            unsealUrlConnection.setDoOutput(true);
            unsealUrlConnection.setRequestMethod("PUT");

            String unsealData = "{\"key\": \"" + unsealKey + "\"}";

            try(OutputStreamWriter writer = new OutputStreamWriter(unsealUrlConnection.getOutputStream())) {
                writer.write(unsealData);
            }

            unsealUrlConnection.connect();
            assertThat(unsealUrlConnection.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_OK);
        });

        Given("the vault is initialised and unsealed", () -> {
            final URL initUrl = UriBuilder.fromUri("https://localhost:8200").path("v1/sys/health").build().toURL();
            HttpURLConnection initUrlConnection = (HttpURLConnection) initUrl.openConnection();
            initUrlConnection.connect();

            // See https://www.vaultproject.io/api/system/health.html for info on response codes for this endpoint
            assertThat(initUrlConnection.getResponseCode()).as("check vault is initialized").isNotEqualTo(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
            assertThat(initUrlConnection.getResponseCode()).as("check vault is unsealed").isNotEqualTo(503);
            assertThat(initUrlConnection.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_OK);
        });

        Given("the vault contains a key pair", () -> {
            Objects.requireNonNull(vaultToken);

            final URL setSecretUrl = UriBuilder.fromUri("http://127.0.0.1:8200").path("v1/secret/data/tessera").build().toURL();
            HttpURLConnection setSecretUrlConnection = (HttpURLConnection) setSecretUrl.openConnection();

            setSecretUrlConnection.setDoOutput(true);
            setSecretUrlConnection.setRequestMethod("POST");
            setSecretUrlConnection.setRequestProperty("X-Vault-Token", vaultToken);

            setSecretUrlConnection.connect();

            String setSecretData = "{\"data\": {\"publicKey\": \"/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=\", \"privateKey\": \"yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=\"}}";

            try(OutputStreamWriter writer = new OutputStreamWriter(setSecretUrlConnection.getOutputStream())) {
                writer.write(setSecretData);
            }

            assertThat(setSecretUrlConnection.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_OK);

            final URL getSecretUrl = UriBuilder.fromUri("http://127.0.0.1:8200").path("v1/secret/data/tessera").build().toURL();
            HttpURLConnection getSecretUrlConnection = (HttpURLConnection) getSecretUrl.openConnection();
            getSecretUrlConnection.setRequestProperty("X-Vault-Token", vaultToken);
            getSecretUrlConnection.connect();

            int getSecretResponseCode = getSecretUrlConnection.getResponseCode();
            assertThat(getSecretResponseCode).isEqualTo(HttpURLConnection.HTTP_OK);

            JsonReader jsonReader = Json.createReader(getSecretUrlConnection.getInputStream());

            JsonObject getSecretObject = jsonReader.readObject();
            JsonObject keyDataObject = getSecretObject.getJsonObject("data").getJsonObject("data");
            assertThat(keyDataObject.getString("publicKey")).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
            assertThat(keyDataObject.getString("privateKey")).isEqualTo("yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");
        });

        Given("the configfile contains the correct vault configuration", () -> {
            URL configFile = getClass().getResource("/vault/hashicorp-config.json");

            final Config config = JaxbUtil.unmarshal(configFile.openStream(), Config.class);

            HashicorpKeyVaultConfig expectedVaultConfig = new HashicorpKeyVaultConfig();
            expectedVaultConfig.setUrl("http://127.0.0.1:8200");

            assertThat(config.getKeys().getHashicorpKeyVaultConfig()).isEqualToComparingFieldByField(expectedVaultConfig);
        });

        Given("the configfile contains the correct key data", () -> {
            URL configFile = getClass().getResource("/vault/hashicorp-config.json");

            final Config config = JaxbUtil.unmarshal(configFile.openStream(), Config.class);

            HashicorpVaultKeyPair expectedKeyData = new HashicorpVaultKeyPair("publicKey", "privateKey", "secret", "tessera", null);

            assertThat(config.getKeys().getKeyData().size()).isEqualTo(1);
            assertThat(config.getKeys().getKeyData().get(0)).isEqualToComparingFieldByField(expectedKeyData);
        });

        When("Tessera is started", () -> {
            Objects.requireNonNull(vaultToken);

            final String jarfile = System.getProperty("application.jar");

            URL configFile = getClass().getResource("/vault/hashicorp-config.json");
            Path pid = Paths.get(System.getProperty("java.io.tmpdir"), "pidA.pid");

            final URL logbackConfigFile = ProcessManager.class.getResource("/logback-node.xml");

            List<String> args = Arrays.asList(
                "java",
                "-Dspring.profiles.active=disable-unixsocket,disable-sync-poller",
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

            ProcessBuilder tesseraProcessBuilder = new ProcessBuilder(args);

            Map<String, String> tesseraEnvironment = tesseraProcessBuilder.environment();
            tesseraEnvironment.put("HASHICORP_TOKEN", vaultToken);

            tesseraProcess.set(
                tesseraProcessBuilder.redirectErrorStream(true)
                    .start()
            );

            executorService.submit(() -> {

                try(BufferedReader reader = Stream.of(tesseraProcess.get().getInputStream())
                    .map(InputStreamReader::new)
                    .map(BufferedReader::new)
                    .findAny().get()) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            final Config config = JaxbUtil.unmarshal(configFile.openStream(), Config.class);

            final URL bindingUrl = UriBuilder.fromUri(config.getP2PServerConfig().getBindingUri()).path("upcheck").build().toURL();

            CountDownLatch startUpLatch = new CountDownLatch(1);

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

            boolean started = startUpLatch.await(30, TimeUnit.SECONDS);

            if (!started) {
                System.err.println(bindingUrl + " Not started. ");
            }

            executorService.submit(() -> {
                try {
                    int exitCode = tesseraProcess.get().waitFor();
                    if (0 != exitCode) {
                        System.err.println("Tessera node exited with code " + exitCode);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });

            startUpLatch.await(30, TimeUnit.SECONDS);
        });

        When("Tessera keygen is used with the Hashicorp options provided", () -> {
            Objects.requireNonNull(vaultToken);

            final String jarfile = System.getProperty("application.jar");

            final URL logbackConfigFile = ProcessManager.class.getResource("/logback-node.xml");

            List<String> args = Arrays.asList(
                "java",
                "-Dspring.profiles.active=disable-unixsocket,disable-sync-poller",
                "-Dlogback.configurationFile=" + logbackConfigFile.getFile(),
                "-Ddebug=true",
                "-jar",
                jarfile,
                "-keygen",
                "-keygenvaulturl",
                "http://127.0.0.1:8200",
                "-keygenvaulttype",
                "hashicorp",
                "-filename",
                "tessera/nodeA,tessera/nodeB",
                "-keygenvaultsecretengine",
                "secret"
            );

            System.out.println(String.join(" ", args));

            ProcessBuilder tesseraProcessBuilder = new ProcessBuilder(args);

            Map<String, String> tesseraEnvironment = tesseraProcessBuilder.environment();
            tesseraEnvironment.put("HASHICORP_TOKEN", vaultToken);

            tesseraProcess.set(
                tesseraProcessBuilder.redirectErrorStream(true)
                    .start()
            );

            executorService.submit(() -> {

                try(BufferedReader reader = Stream.of(tesseraProcess.get().getInputStream())
                    .map(InputStreamReader::new)
                    .map(BufferedReader::new)
                    .findAny().get()) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            CountDownLatch startUpLatch = new CountDownLatch(1);
            startUpLatch.await(10, TimeUnit.SECONDS);
        });

        Then("Tessera will retrieve the key pair from the vault", () -> {
            final URL partyInfoUrl = UriBuilder.fromUri("http://127.0.0.1")
                .port(8080)
                .path("partyinfo")
                .build()
                .toURL();

            HttpURLConnection partyInfoUrlConnection = (HttpURLConnection) partyInfoUrl.openConnection();
            partyInfoUrlConnection.connect();

            int partyInfoResponseCode = partyInfoUrlConnection.getResponseCode();
            assertThat(partyInfoResponseCode).isEqualTo(HttpURLConnection.HTTP_OK);

            JsonReader jsonReader = Json.createReader(partyInfoUrlConnection.getInputStream());

            JsonObject partyInfoObject = jsonReader.readObject();

            assertThat(partyInfoObject).isNotNull();
            assertThat(partyInfoObject.getJsonArray("keys")).hasSize(1);
            assertThat(partyInfoObject.getJsonArray("keys").getJsonObject(0).getString("key")).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        });

        Then("a new key pair {string} will be added to the vault", (String secretName) -> {
            Objects.requireNonNull(vaultToken);

            final URL getSecretUrl = UriBuilder.fromUri("http://127.0.0.1:8200")
                .path("v1/secret/data/" + secretName)
                .build()
                .toURL();

            HttpURLConnection getSecretUrlConnection = (HttpURLConnection) getSecretUrl.openConnection();
            getSecretUrlConnection.setRequestProperty("X-Vault-Token", vaultToken);
            getSecretUrlConnection.connect();

            int getSecretResponseCode = getSecretUrlConnection.getResponseCode();
            assertThat(getSecretResponseCode).isEqualTo(HttpURLConnection.HTTP_OK);

            JsonReader jsonReader = Json.createReader(getSecretUrlConnection.getInputStream());

            JsonObject getSecretObject = jsonReader.readObject();
            JsonObject keyDataObject = getSecretObject.getJsonObject("data").getJsonObject("data");

            assertThat(keyDataObject.size()).isEqualTo(2);
            assertThat(keyDataObject.getString("publicKey")).isNotBlank();
            assertThat(keyDataObject.getString("privateKey")).isNotBlank();
        });

        After(() -> {
            if(vaultServerProcess.get() != null && vaultServerProcess.get().isAlive()) {
                vaultServerProcess.get().destroy();
            }

            if(tesseraProcess.get() != null && tesseraProcess.get().isAlive()) {
                tesseraProcess.get().destroy();
            }
        });
    }

}
