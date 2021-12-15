package com.quorum.tessera.multitenancy.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.data.EncryptedRawTransaction;
import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import picocli.CommandLine;

@RunWith(Parameterized.class)
public class MigrationTest {

  @Rule public TemporaryFolder workDir = new TemporaryFolder();

  private Path primaryConfigPath;

  private Path secondaryConfigPath;

  private List<String> args;

  private EntityManagerFactory primaryEntityManagerFactory;

  private EntityManagerFactory secondaryEntityManagerFactory;

  private int encryptedTransactionCount;

  private int encryptedRawTransactionCount;

  public MigrationTest(TestInfo testInfo) {
    this.encryptedTransactionCount = testInfo.getEncryptedTransactionCount();
    this.encryptedRawTransactionCount = testInfo.getEncryptedRawTransactionCount();
  }

  @Before
  public void beforeTest() throws IOException {

    Config primaryConfig = new Config();
    primaryConfig.setJdbcConfig(new JdbcConfig());
    primaryConfig.getJdbcConfig().setUsername("junit");
    primaryConfig.getJdbcConfig().setPassword("junit");
    String primaryJdbcUrl =
        "jdbc:h2:" + workDir.getRoot().toPath().resolve("primary.db").toString();
    primaryConfig.getJdbcConfig().setUrl(primaryJdbcUrl);

    Config secondaryConfig = new Config();
    secondaryConfig.setJdbcConfig(new JdbcConfig());
    secondaryConfig.getJdbcConfig().setUsername("junit");
    secondaryConfig.getJdbcConfig().setPassword("junit");
    String secondaryJdbcUrl =
        "jdbc:h2:" + workDir.getRoot().toPath().resolve("secondary.db").toString();
    secondaryConfig.getJdbcConfig().setUrl(secondaryJdbcUrl);

    primaryConfigPath = workDir.getRoot().toPath().toAbsolutePath().resolve("primary-confg.json");
    try (OutputStream outputStream = Files.newOutputStream(primaryConfigPath)) {
      JaxbUtil.marshalWithNoValidation(primaryConfig, outputStream);
    }

    secondaryConfigPath =
        workDir.getRoot().toPath().toAbsolutePath().resolve("secondary-confg.json");
    try (OutputStream outputStream = Files.newOutputStream(secondaryConfigPath)) {
      JaxbUtil.marshalWithNoValidation(secondaryConfig, outputStream);
    }

    args =
        List.of(
            "--primary",
            primaryConfigPath.toString(),
            "--secondary",
            secondaryConfigPath.toString());

    primaryEntityManagerFactory =
        Optional.of(primaryConfig)
            .map(Config::getJdbcConfig)
            .map(JdbcConfigUtil::toMap)
            .map(m -> new HashMap(m))
            .map(
                p -> {
                  p.put("jakarta.persistence.schema-generation.database.action", "drop-and-create");
                  EntityManagerFactory emf = Persistence.createEntityManagerFactory("tessera", p);
                  emf.createEntityManager();
                  return emf;
                })
            .get();

    secondaryEntityManagerFactory =
        Optional.of(secondaryConfig)
            .map(Config::getJdbcConfig)
            .map(JdbcConfigUtil::toMap)
            .map(m -> new HashMap(m))
            .map(
                p -> {
                  p.put("jakarta.persistence.schema-generation.database.action", "create");
                  EntityManagerFactory emf = Persistence.createEntityManagerFactory("tessera", p);
                  return emf;
                })
            .get();

    EntityManager secondaryEntityManager = secondaryEntityManagerFactory.createEntityManager();
    secondaryEntityManager.getTransaction().begin();
    IntStream.range(0, encryptedTransactionCount)
        .forEach(
            i -> {
              EncryptedTransaction encryptedTransaction = generateEncryptedTransaction();
              secondaryEntityManager.persist(encryptedTransaction);
            });
    secondaryEntityManager.getTransaction().commit();

    secondaryEntityManager.getTransaction().begin();
    IntStream.range(0, encryptedRawTransactionCount)
        .forEach(
            i -> {
              EncryptedRawTransaction encryptedRawTransaction = generateEncryptedRawTransaction();
              secondaryEntityManager.persist(encryptedRawTransaction);
            });
    secondaryEntityManager.getTransaction().commit();
  }

  @After
  public void afterTest() {
    primaryEntityManagerFactory.close();
    secondaryEntityManagerFactory.close();
  }

  @Test
  public void doMigration() {

    MigrationCliAdapter migrationCommand = new MigrationCliAdapter();
    assertThat(migrationCommand.getType()).isEqualTo(CliType.MULTITENANCY_MIGRATION);

    final CommandLine commandLine = new CommandLine(migrationCommand);
    commandLine
        .registerConverter(Config.class, new ConfigConverter())
        .setSeparator(" ")
        .setCaseInsensitiveEnumValuesAllowed(true);

    int exitCode = commandLine.execute(args.toArray(String[]::new));
    assertThat(exitCode).isZero();

    EntityManager secondaryEntityManager = secondaryEntityManagerFactory.createEntityManager();
    EntityManager primaryEntityManager = primaryEntityManagerFactory.createEntityManager();

    secondaryEntityManager.getTransaction().begin();
    primaryEntityManager.getTransaction().begin();

    secondaryEntityManager
        .createQuery("select count(e) from EncryptedTransaction e", Long.class)
        .getResultStream()
        .findFirst()
        .ifPresent(count -> assertThat(count).isEqualTo(encryptedTransactionCount));

    primaryEntityManager
        .createQuery("select count(e) from EncryptedTransaction e", Long.class)
        .getResultStream()
        .findFirst()
        .ifPresent(count -> assertThat(count).isEqualTo(encryptedTransactionCount));

    secondaryEntityManager
        .createQuery("select count(e) from EncryptedRawTransaction e", Long.class)
        .getResultStream()
        .findFirst()
        .ifPresent(count -> assertThat(count).isEqualTo(encryptedRawTransactionCount));

    primaryEntityManager
        .createQuery("select count(e) from EncryptedRawTransaction e", Long.class)
        .getResultStream()
        .findFirst()
        .ifPresent(count -> assertThat(count).isEqualTo(encryptedRawTransactionCount));

    secondaryEntityManager
        .createQuery("select e from EncryptedTransaction e", EncryptedTransaction.class)
        .getResultStream()
        .forEach(
            e -> {
              EncryptedTransaction copiedEncryptedTransaction =
                  primaryEntityManager.find(EncryptedTransaction.class, e.getHash());
              assertThat(copiedEncryptedTransaction).isNotNull();
              assertThat(copiedEncryptedTransaction.getEncodedPayload())
                  .isEqualTo(e.getEncodedPayload());
            });

    secondaryEntityManager
        .createQuery("select e from EncryptedRawTransaction e", EncryptedRawTransaction.class)
        .getResultStream()
        .forEach(
            e -> {
              EncryptedRawTransaction copiedEncryptedRawTransaction =
                  primaryEntityManager.find(EncryptedRawTransaction.class, e.getHash());
              assertThat(copiedEncryptedRawTransaction).isNotNull();
              assertThat(copiedEncryptedRawTransaction.getEncryptedKey())
                  .isEqualTo(e.getEncryptedKey());
              assertThat(copiedEncryptedRawTransaction.getEncryptedPayload())
                  .isEqualTo(e.getEncryptedPayload());
              assertThat(copiedEncryptedRawTransaction.getSender()).isEqualTo(e.getSender());
              assertThat(copiedEncryptedRawTransaction.getNonce()).isEqualTo(e.getNonce());
            });

    secondaryEntityManager.getTransaction().rollback();
    primaryEntityManager.getTransaction().rollback();

    assertThat(commandLine.execute(args.toArray(String[]::new)))
        .describedAs("Rerunning should throw no errors as there are exist checks before insert")
        .isZero();

    primaryEntityManager
        .createQuery("select count(e) from EncryptedTransaction e", Long.class)
        .getResultStream()
        .findFirst()
        .ifPresent(count -> assertThat(count).isEqualTo(encryptedTransactionCount));

    secondaryEntityManager
        .createQuery("select count(e) from EncryptedRawTransaction e", Long.class)
        .getResultStream()
        .findFirst()
        .ifPresent(count -> assertThat(count).isEqualTo(encryptedRawTransactionCount));
  }

  static EncryptedTransaction generateEncryptedTransaction() {
    EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setHash(new MessageHash(UUID.randomUUID().toString().getBytes()));
    encryptedTransaction.setPayload(generateEncodedPayload());
    encryptedTransaction.setEncodedPayloadCodec(EncodedPayloadCodec.LEGACY);
    return encryptedTransaction;
  }

  static EncodedPayload generateEncodedPayload() {

    PrivacyMode privacyMode =
        Arrays.stream(PrivacyMode.values())
            .skip((int) (PrivacyMode.values().length * Math.random()))
            .findAny()
            .get();

    PublicKey senderKey = PublicKey.from("SenderKey".getBytes());

    EncodedPayload.Builder encodedPayloadBuilder =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText("cipherText".getBytes())
            .withCipherTextNonce("CipherTextNonce".getBytes())
            .withPrivacyMode(privacyMode)
            .withRecipientNonce("RecipientNonce".getBytes())
            .withRecipientKeys(List.of(senderKey, PublicKey.from("Recipient".getBytes())));

    if (privacyMode != PrivacyMode.PRIVATE_STATE_VALIDATION) {
      if (privacyMode == PrivacyMode.MANDATORY_RECIPIENTS) {
        encodedPayloadBuilder.withMandatoryRecipients(
            Set.of(PublicKey.from("Recipient".getBytes())));
      }
      encodedPayloadBuilder.withExecHash(new byte[0]);
    } else {
      encodedPayloadBuilder.withExecHash("execHash".getBytes());
    }

    return encodedPayloadBuilder.build();
  }

  static EncryptedRawTransaction generateEncryptedRawTransaction() {
    final EncryptedRawTransaction secondaryRawTx =
        new EncryptedRawTransaction(
            new MessageHash(UUID.randomUUID().toString().getBytes()),
            "some encrypted message".getBytes(),
            "encryptedKey".getBytes(),
            "nonce".getBytes(),
            "sender".getBytes());
    return secondaryRawTx;
  }

  @Parameterized.Parameters(name = "{0}")
  public static List<TestInfo> configs() {
    return List.of(new TestInfo(21, 89), new TestInfo(91, 12));
  }

  static class TestInfo {

    private int encryptedTransactionCount;

    private int encryptedRawTransactionCount;

    TestInfo(int encryptedTransactionCount, int encryptedRawTransactionCount) {
      this.encryptedTransactionCount = encryptedTransactionCount;
      this.encryptedRawTransactionCount = encryptedRawTransactionCount;
    }

    public int getEncryptedTransactionCount() {
      return encryptedTransactionCount;
    }

    public int getEncryptedRawTransactionCount() {
      return encryptedRawTransactionCount;
    }

    @Override
    public String toString() {
      return "TestInfo{"
          + "encryptedTransactionCount="
          + encryptedTransactionCount
          + ", encryptedRawTransactionCount="
          + encryptedRawTransactionCount
          + '}';
    }
  }
}
