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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class HashicorpStepDefs implements En {

    private static final Logger LOGGER = LoggerFactory.getLogger(HashicorpStepDefs.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private String VAULTTOKEN;

    public HashicorpStepDefs() {
        //TODO If a vault process is already running, then stop it
        //TODO If the test fails, make sure the vault process is killed
        //TODO Use HTTP API instead of vault client to check the vault is up

        Given("the dev vault server has been started", () -> {

            ProcessBuilder vaultServerProcessBuilder = new ProcessBuilder("vault", "server", "-dev");

            Process vaultServerProcess = vaultServerProcessBuilder.redirectErrorStream(true)
                                                      .start();

            AtomicBoolean isAddressAlreadyInUse = new AtomicBoolean(false);

            executorService.submit(() -> {
                try(BufferedReader reader = Stream.of(vaultServerProcess.getInputStream())
                                                  .map(InputStreamReader::new)
                                                  .map(BufferedReader::new)
                                                  .findAny().get()) {

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        //TODO The continual output of this interferes with the other processes/threads - how to resolve?
                        System.out.println(line);
                        if(line.matches("^Error.+address already in use")) {
                            isAddressAlreadyInUse.set(true);
                        }

                        if(line.matches("^Root Token: .+$")) {
                            String[] components = line.split(" ");
                            VAULTTOKEN = components[components.length-1].trim();
                        }
                    }

                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            // wait so that assertion is not evaluated before output is checked
            CountDownLatch startUpLatch = new CountDownLatch(1);
            boolean started = startUpLatch.await(5, TimeUnit.SECONDS);

            assertThat(isAddressAlreadyInUse).isFalse();

        });

        Given("the vault is initialised and unsealed", () -> {

            ProcessBuilder vaultClientProcessBuilder = new ProcessBuilder("vault", "status");
            Map<String, String> vaultClientEnvironment = vaultClientProcessBuilder.environment();
            vaultClientEnvironment.put("VAULT_ADDR", "http://127.0.0.1:8200");

            Process vaultClientProcess = vaultClientProcessBuilder.redirectErrorStream(true)
                                                                  .start();

            final AtomicBoolean isVaultInitialised = new AtomicBoolean(false);
            final AtomicBoolean isVaultSealed = new AtomicBoolean(true);

            executorService.submit(() -> {
                try(BufferedReader reader = Stream.of(vaultClientProcess.getInputStream())
                                                  .map(InputStreamReader::new)
                                                  .map(BufferedReader::new)
                                                  .findAny().get()) {

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);

                        if(line.matches("^Initialized.+true$")) {
                            isVaultInitialised.set(true);
                        }

                        if(line.matches("^Sealed.+false$")) {
                            isVaultSealed.set(false);
                        }
                    }

                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            vaultClientProcess.waitFor();

            assertThat(isVaultInitialised).isTrue();
            assertThat(isVaultSealed).isFalse();

        });

        Given("the v1 kv secret engine is enabled", () -> {

            Objects.requireNonNull(VAULTTOKEN);

            List<String> args = Arrays.asList(
                "vault",
                "secrets",
                "enable",
                "-version=1",
                "-path=secret-v1",
                "kv"
            );

            ProcessBuilder vaultClientProcessBuilder = new ProcessBuilder(args);
            Map<String, String> vaultClientEnvironment = vaultClientProcessBuilder.environment();
            vaultClientEnvironment.put("VAULT_ADDR", "http://127.0.0.1:8200");
            vaultClientEnvironment.put("VAULT_DEV_ROOT_TOKEN_ID", VAULTTOKEN);

            Process vaultClientProcess = vaultClientProcessBuilder.redirectErrorStream(true)
                                                                  .start();

            final AtomicBoolean wasSuccessful = new AtomicBoolean();
            wasSuccessful.set(false);

            executorService.submit(() -> {
                try(BufferedReader reader = Stream.of(vaultClientProcess.getInputStream())
                                                  .map(InputStreamReader::new)
                                                  .map(BufferedReader::new)
                                                  .findAny().get()) {

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);

                        if(line.matches("^Success! Enabled the kv secrets engine at: secret-v1/$")) {
                            wasSuccessful.set(true);
                        }
                    }

                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            vaultClientProcess.waitFor();

            assertThat(wasSuccessful).isTrue();
        });

        Given("the vault contains a key pair", () -> {
            Objects.requireNonNull(VAULTTOKEN);

            List<String> args = Arrays.asList(
                "vault",
                "kv",
                "put",
                "secret-v1/tessera",
                "publicKey=/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=",
                "privateKey=yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM="
            );

            ProcessBuilder vaultClientProcessBuilder = new ProcessBuilder(args);
            Map<String, String> vaultClientEnvironment = vaultClientProcessBuilder.environment();
            vaultClientEnvironment.put("VAULT_ADDR", "http://127.0.0.1:8200");
            vaultClientEnvironment.put("VAULT_DEV_ROOT_TOKEN_ID", VAULTTOKEN);

            Process vaultClientProcess = vaultClientProcessBuilder.redirectErrorStream(true)
                                                                  .start();

            final AtomicBoolean wasSuccessful = new AtomicBoolean();
            wasSuccessful.set(false);

            executorService.submit(() -> {
                try(BufferedReader reader = Stream.of(vaultClientProcess.getInputStream())
                                                  .map(InputStreamReader::new)
                                                  .map(BufferedReader::new)
                                                  .findAny().get()) {

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);

                        if(line.matches("^Success! Data written to: secret-v1/tessera$")) {
                            wasSuccessful.set(true);
                        }
                    }

                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            vaultClientProcess.waitFor();

            List<String> getArgs = Arrays.asList(
                "vault",
                "kv",
                "get",
                "secret-v1/tessera"
            );

            ProcessBuilder getSecretProcessBuilder = new ProcessBuilder(getArgs);
            Map<String, String> getSecretEnvironment = getSecretProcessBuilder.environment();
            getSecretEnvironment.put("VAULT_ADDR", "http://127.0.0.1:8200");
            getSecretEnvironment.put("VAULT_DEV_ROOT_TOKEN_ID", VAULTTOKEN);

            Process getSecretProcess = getSecretProcessBuilder.redirectErrorStream(true)
                                                              .start();

            final AtomicBoolean isPublicKeySet = new AtomicBoolean();
            isPublicKeySet.set(false);

            final AtomicBoolean isPrivateKeySet = new AtomicBoolean();
            isPrivateKeySet.set(false);

            executorService.submit(() -> {
                try(BufferedReader reader = Stream.of(getSecretProcess.getInputStream())
                                                  .map(InputStreamReader::new)
                                                  .map(BufferedReader::new)
                                                  .findAny().get()) {

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        String[] splitLine = line.split("\\s+");

                        if(line.contains("publicKey") && splitLine.length == 2) {
                            if(splitLine[1].equals("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")) {
                                isPublicKeySet.set(true);
                            }
                        }

                        if(line.contains("privateKey") && splitLine.length == 2) {
                            if(splitLine[1].equals("yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=")) {
                                isPrivateKeySet.set(true);
                            }
                        }
                    }

                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

            getSecretProcess.waitFor();

            assertThat(isPublicKeySet).isTrue();
            assertThat(isPrivateKeySet).isTrue();
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

            HashicorpVaultKeyPair expectedKeyData = new HashicorpVaultKeyPair("publicKey", "privateKey", "secret-v1/tessera");

            assertThat(config.getKeys().getKeyData().size()).isEqualTo(1);
            assertThat(config.getKeys().getKeyData().get(0)).isEqualToComparingFieldByField(expectedKeyData);
        });

        When("Tessera is started", () -> {

            final URL logbackConfigFile = ProcessManager.class.getResource("/logback-node.xml");

            URL configFile = getClass().getResource("/vault/hashicorp-config.json");

            final Path jarfile = Paths.get("/Users/chrishounsom/jpmc-tessera/tessera-app/target/tessera-app-0.8-SNAPSHOT-app.jar");

            List<String> tesseraArgs = Arrays.asList(
                "java",
                "-Dspring.profiles.active=disable-unixsocket,disable-sync-poller",
                "-Dlogback.configurationFile=" + logbackConfigFile.getFile(),
                "-Ddebug=true",
                "-jar",
                jarfile.toString(),
                "-configfile",
                ElUtil.createAndPopulatePaths(configFile).toAbsolutePath().toString(),
//                "-pidfile",
//                pid.toAbsolutePath().toString(),
                "-jdbc.autoCreateTables", "true"
            );
            System.out.println(String.join(" ", tesseraArgs));

            ProcessBuilder tesseraProcessBuilder = new ProcessBuilder(tesseraArgs);

            Process tesseraProcess = tesseraProcessBuilder.redirectErrorStream(true)
                                                                  .start();

            executorService.submit(() -> {
                try(BufferedReader reader = Stream.of(tesseraProcess.getInputStream())
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
        });

        Then("Tessera will have retrieved the key pair from the vault", () -> {
            // Write code here that turns the phrase above into concrete actions
            throw new cucumber.api.PendingException();
        });

    }

}
