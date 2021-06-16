package com.quorum.tessera.test.cli.keygen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keypairs.InlineKeypair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.encryption.SharedKey;
import exec.ExecArgsBuilder;
import io.cucumber.java8.En;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileKeygenSteps implements En {

  private static final Base64.Decoder DECODER = Base64.getDecoder();

  private final ExecutorService executorService = Executors.newFixedThreadPool(2);

  // this is used to check the generated keys against, to produce the SharedKey
  // private final KeyPair knownGoodKeypair =
  // transaction.utils.Utils.getEncryptor().generateNewKeys();
  private Path buildDir;

  private String password;

  private List<String> args;

  private Integer exitCode;

  private Path publicKeyPath;

  private Path privateKeyPath;

  public FileKeygenSteps() {

    Given(
        "the application is available",
        () -> {
          this.exitCode = null;
          this.buildDir = Files.createTempDirectory(UUID.randomUUID().toString());

          final String appPath = System.getProperty("application.jar");

          if (Objects.equals("", appPath)) {
            throw new IllegalStateException("No application.jar system property defined");
          }

          final Path startScript = Paths.get(appPath);

          this.args =
              new ExecArgsBuilder()
                  .withStartScript(startScript)
                  .withArg("-keygen")
                  .withArg("--encryptor.type", "NACL")
                  .build();
        });

    Given("no file exists at {string}", (String path) -> Files.deleteIfExists(Paths.get(path)));

    Given("no password", () -> this.password = "");

    Given("a password of {string}", (String password) -> this.password = password);

    // here to explicitly state we are doing nothing
    Given("no file path is provided", () -> {});

    Given(
        "a file path of {string}",
        (String path) -> this.args.addAll(Arrays.asList("-filename", path)));

    When(
        "new keys are generated",
        () -> {
          final ProcessBuilder processBuilder = new ProcessBuilder(args);
          processBuilder.directory(buildDir.toFile());

          final Process process = processBuilder.start();
          executorService.submit(
              new FileKeygenStreamConsumer(
                  process.getInputStream(), process.getOutputStream(), password));
          executorService.submit(
              new FileKeygenStreamConsumer(
                  process.getErrorStream(), process.getOutputStream(), password));

          this.exitCode = process.waitFor();
        });

    Then(
        "the application has an exit code of {int}",
        (Integer expectedExitCode) -> assertThat(exitCode).isEqualTo(expectedExitCode));

    And(
        "public key file {string} exists and private key file {string} exists",
        (String publicKeyPath, String privateKeyPath) -> {
          this.publicKeyPath = buildDir.resolve(publicKeyPath);
          this.privateKeyPath = buildDir.resolve(privateKeyPath);

          assertThat(this.publicKeyPath).exists();
          assertThat(this.privateKeyPath).exists();
        });

    And(
        "the generated keys are valid",
        () -> {
          final Encryptor naclFacade = transaction.utils.Utils.getEncryptor(EncryptorType.NACL);
          KeyPair knownGoodKeypair = naclFacade.generateNewKeys();

          InlineKeypair inlineKeypair = mock(InlineKeypair.class);

          String encodedPublicKey = knownGoodKeypair.getPublicKey().encodeToBase64();
          String encodedPrivateKey = knownGoodKeypair.getPrivateKey().encodeToBase64();

          when(inlineKeypair.getPublicKey()).thenReturn(encodedPublicKey);
          when(inlineKeypair.getPrivateKey()).thenReturn(encodedPrivateKey);

          KeyEncryptor keyEncryptor =
              KeyEncryptorFactory.newFactory()
                  .create(
                      new EncryptorConfig() {
                        {
                          setType(EncryptorType.NACL);
                        }
                      });

          final FilesystemKeyPair generatedKeys =
              new FilesystemKeyPair(this.publicKeyPath, this.privateKeyPath, keyEncryptor);
          generatedKeys.withPassword(this.password.toCharArray());

          final PublicKey publicKey = PublicKey.from(DECODER.decode(generatedKeys.getPublicKey()));
          final PrivateKey privateKey =
              PrivateKey.from(DECODER.decode(generatedKeys.getPrivateKey()));

          final SharedKey firstKey =
              naclFacade.computeSharedKey(publicKey, knownGoodKeypair.getPrivateKey());
          final SharedKey secondKey =
              naclFacade.computeSharedKey(knownGoodKeypair.getPublicKey(), privateKey);

          assertThat(firstKey).isEqualTo(secondKey);
        });
  }
}
