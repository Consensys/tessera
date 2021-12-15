package com.quorum.tessera.data.staging.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.TestConfig;
import com.quorum.tessera.data.Utils;
import com.quorum.tessera.data.staging.StagingAffectedTransaction;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransaction;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockedStatic;

@RunWith(Parameterized.class)
public class StagingEntityDAOTest {

  private EntityManagerFactory entityManagerFactory;

  private StagingEntityDAO stagingEntityDAO;

  private Map<String, StagingTransaction> transactions;

  private TestConfig testConfig;

  private MockedStatic<PayloadEncoder> mockedStaticPayloadEncoder;

  private PayloadEncoder payloadEncoder;

  private byte[] payloadData;

  private EncodedPayload encodedPayload;

  final EncodedPayloadCodec CODEC = EncodedPayloadCodec.current();

  public StagingEntityDAOTest(TestConfig testConfig) {
    this.testConfig = testConfig;
  }

  @Before
  public void beforeTest() throws Exception {

    mockedStaticPayloadEncoder = mockStatic(PayloadEncoder.class);
    payloadData = "I LOve Sparrows".getBytes();
    encodedPayload = mock(EncodedPayload.class);
    payloadEncoder = mock(PayloadEncoder.class);

    mockedStaticPayloadEncoder
        .when(() -> PayloadEncoder.create(any(EncodedPayloadCodec.class)))
        .thenReturn(payloadEncoder);
    when(payloadEncoder.decode(payloadData)).thenReturn(encodedPayload);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

    Map properties = new HashMap();
    properties.put("jakarta.persistence.jdbc.url", testConfig.getUrl());
    properties.put("jakarta.persistence.jdbc.user", "junit");
    properties.put("jakarta.persistence.jdbc.password", "");
    properties.put(
        "eclipselink.logging.logger", "org.eclipse.persistence.logging.slf4j.SLF4JLogger");
    properties.put("eclipselink.logging.level", "FINE");
    properties.put("eclipselink.logging.parameters", "true");
    properties.put("eclipselink.logging.level.sql", "FINE");
    properties.put("jakarta.persistence.schema-generation.database.action", "drop-and-create");
    properties.put("eclipselink.cache.shared.default", "false");
    properties.put(
        "eclipselink.session.customizer", "com.quorum.tessera.eclipselink.AtomicLongSequence");

    entityManagerFactory = Persistence.createEntityManagerFactory("tessera-recover", properties);

    stagingEntityDAO = new StagingEntityDAOImpl(entityManagerFactory);

    transactions = createFixtures();
  }

  @After
  public void afterTest() throws Exception {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    //    entityManager.createQuery("delete from StagingTransactionVersion").executeUpdate();
    entityManager.createQuery("delete from StagingAffectedTransaction").executeUpdate();
    // entityManager.createQuery("delete from StagingRecipient").executeUpdate();
    entityManager.createQuery("delete from StagingTransaction").executeUpdate();
    entityManager.getTransaction().commit();
    transactions.clear();

    mockedStaticPayloadEncoder.close();
  }

  @Test
  public void updateStageForBatch() {

    final long validationStage = new Random().nextLong();
    final int batchSize = 1;

    int results = stagingEntityDAO.updateStageForBatch(batchSize, validationStage);
    assertThat(results).isEqualTo(batchSize);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    Root<StagingTransaction> root = criteriaQuery.from(StagingTransaction.class);
    criteriaQuery
        .select(criteriaBuilder.count(root))
        .where(criteriaBuilder.equal(root.get("validationStage"), validationStage));

    Long countPending =
        entityManager
            .createQuery(criteriaQuery)
            .setParameter("stage", validationStage)
            .getSingleResult();

    assertThat(countPending).isEqualTo((long) batchSize);
  }

  @Test
  public void testStagingQuery() {

    final List<StagingTransaction> preStaging =
        stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);

    final AtomicLong stage = new AtomicLong(0);
    final int batchSize = 10;

    assertThat(preStaging.size()).isEqualTo(7);
    preStaging.forEach(
        stagingTransaction -> {
          assertThat(stagingTransaction.getValidationStage()).isNull();
        });

    while (stagingEntityDAO.updateStageForBatch(batchSize, stage.incrementAndGet()) != 0) {}

    final List<StagingTransaction> verifiedTransactions =
        stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);

    // First tx to process will need to be Tx1
    assertThat(verifiedTransactions.get(0).getValidationStage()).isEqualTo(1L);
    assertThat(verifiedTransactions.get(0).getId()).isEqualTo(1L);

    // Then tx2 (2 versions) and 3
    assertThat(verifiedTransactions.get(1).getValidationStage()).isEqualTo(2L);
    assertThat(verifiedTransactions.get(2).getValidationStage()).isEqualTo(2L);
    assertThat(verifiedTransactions.get(3).getValidationStage()).isEqualTo(2L);

    // Then transaction 4 as its affected tx (3) had been validated
    assertThat(verifiedTransactions.get(4).getValidationStage()).isEqualTo(3L);
    assertThat(verifiedTransactions.get(4).getId()).isEqualTo(4L);

    // Then transaction 7 as all of its affected txs (1 and 4) had been validated
    assertThat(verifiedTransactions.get(5).getValidationStage()).isEqualTo(4L);
    assertThat(verifiedTransactions.get(5).getId()).isEqualTo(7L);

    // Transaction 5 can never be validated as it depends on an unknown tx6
    assertThat(verifiedTransactions.get(6).getValidationStage()).isNull();
    assertThat(verifiedTransactions.get(6).getId()).isEqualTo(5L);

    final List<StagingTransaction> allTransactions =
        stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);

    assertThat(allTransactions.stream().filter(et -> et.getValidationStage() == null).count())
        .isEqualTo(1);
    assertThat(stagingEntityDAO.countAll()).isEqualTo(7);
    assertThat(stagingEntityDAO.countStaged()).isEqualTo(6);

    assertThat(stagingEntityDAO.countAllAffected()).isEqualTo(7);
  }

  @Test
  public void paginationCanCauseDifferentStagingValueButOrderShouldBeMaintained() {

    final List<StagingTransaction> preStaging =
        stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);

    final AtomicLong stage = new AtomicLong(0);
    final int batchSize = 1;

    assertThat(preStaging.size()).isEqualTo(7);
    preStaging.forEach(
        stagingTransaction -> {
          assertThat(stagingTransaction.getValidationStage()).isNull();
        });

    while (stagingEntityDAO.updateStageForBatch(batchSize, stage.incrementAndGet()) != 0) {}

    final List<StagingTransaction> verifiedTransactions =
        stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);

    // Order increase by one due to pagination
    assertThat(verifiedTransactions.get(0).getValidationStage()).isEqualTo(1L);
    assertThat(verifiedTransactions.get(1).getValidationStage()).isEqualTo(2L);
    assertThat(verifiedTransactions.get(2).getValidationStage()).isEqualTo(3L);
    assertThat(verifiedTransactions.get(3).getValidationStage()).isEqualTo(4L);
    assertThat(verifiedTransactions.get(4).getValidationStage()).isEqualTo(5L);
    assertThat(verifiedTransactions.get(5).getValidationStage()).isEqualTo(6L);
    assertThat(verifiedTransactions.get(6).getValidationStage()).isNull();

    final List<String> possibleOrdering =
        Arrays.asList("1,21,22,3,4,7,5", "1,3,21,22,4,7,5", "1,3,4,21,22,7,5", "1,3,4,7,21,22,5");

    final String order =
        verifiedTransactions.stream()
            .map(StagingTransaction::getId)
            .map(String::valueOf)
            .collect(Collectors.joining(","));

    assertThat(possibleOrdering.contains(order));

    final List<StagingTransaction> allTransactions =
        stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);

    assertThat(allTransactions.stream().filter(et -> et.getValidationStage() == null).count())
        .isEqualTo(1);
    assertThat(stagingEntityDAO.countAll()).isEqualTo(7);
    assertThat(stagingEntityDAO.countStaged()).isEqualTo(6);

    assertThat(stagingEntityDAO.countAllAffected()).isEqualTo(7);
  }

  @Test
  public void testRetrieveTransactionByHash() {

    final String txnHash7 = transactions.get("TXN7").getHash();
    final Optional<StagingTransaction> stagingTransaction =
        stagingEntityDAO.retrieveByHash(txnHash7);

    assertThat(stagingTransaction).isPresent();
    assertThat(stagingTransaction.get().getAffectedContractTransactions()).hasSize(2);
  }

  @Test
  public void testUpdate() {

    final String txnHash7 = transactions.get("TXN7").getHash();
    final Optional<StagingTransaction> stagingTransaction =
        stagingEntityDAO.retrieveByHash(txnHash7);

    assertThat(stagingTransaction).isPresent();
    StagingTransaction st = stagingTransaction.get();
    st.setValidationStage(123L);
    stagingEntityDAO.update(st);

    final Optional<StagingTransaction> stagingTransactionAfterUpdate =
        stagingEntityDAO.retrieveByHash(txnHash7);
    assertThat(stagingTransactionAfterUpdate).isPresent();

    assertThat(stagingTransactionAfterUpdate.get().getValidationStage()).isEqualTo(123L);
  }

  @Test
  public void testSave() {

    String txHash = Utils.createHashStr();
    final StagingTransaction stagingTransaction = new StagingTransaction();
    stagingTransaction.setHash(txHash);
    stagingTransaction.setPrivacyMode(PrivacyMode.STANDARD_PRIVATE);
    stagingTransaction.setEncodedPayloadCodec(CODEC);
    stagingTransaction.setPayload(payloadData);

    final StagingAffectedTransaction affected1 = new StagingAffectedTransaction();
    affected1.setSourceTransaction(stagingTransaction);
    affected1.setHash("affected1");
    final StagingAffectedTransaction affected2 = new StagingAffectedTransaction();
    affected2.setId(123L);
    affected2.setSourceTransaction(stagingTransaction);
    affected2.setHash("affected2");

    stagingTransaction.setAffectedContractTransactions(
        Stream.of(affected1, affected2).collect(Collectors.toSet()));

    stagingEntityDAO.save(stagingTransaction);

    assertThat(stagingEntityDAO.retrieveByHash(txHash)).isPresent();

    final StagingTransaction retrieved = stagingEntityDAO.retrieveByHash(txHash).get();

    assertThat(retrieved).isEqualTo(stagingTransaction);
    assertThat(retrieved.getValidationStage()).isNull();
    assertThat(retrieved.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);

    assertThat(retrieved.getAffectedContractTransactions())
        .containsExactlyInAnyOrder(affected1, affected2);

    retrieved.getAffectedContractTransactions().forEach(a -> assertThat(a.getId()).isNotNull());
  }

  public Map<String, StagingTransaction> createFixtures() {

    final EntityManager entityManager = entityManagerFactory.createEntityManager();

    entityManager.getTransaction().begin();

    final String txnHash1 = Utils.createHashStr();

    final StagingTransaction stTransaction1 = new StagingTransaction();
    stTransaction1.setId(1L);
    stTransaction1.setHash(txnHash1);
    stTransaction1.setEncodedPayloadCodec(CODEC);

    entityManager.persist(stTransaction1);

    final String txnHash2 = Utils.createHashStr();

    final StagingTransaction stTransaction2a = new StagingTransaction();
    stTransaction2a.setId(21L);
    stTransaction2a.setHash(txnHash2);
    stTransaction2a.setEncodedPayloadCodec(CODEC);

    StagingAffectedTransaction stAffectedContractTransaction21a = new StagingAffectedTransaction();
    stAffectedContractTransaction21a.setHash(txnHash1);

    stAffectedContractTransaction21a.setSourceTransaction(stTransaction2a);

    stTransaction2a.getAffectedContractTransactions().add(stAffectedContractTransaction21a);

    entityManager.persist(stTransaction2a);

    // Another version of transaction 2
    final StagingTransaction stTransaction2b = new StagingTransaction();
    stTransaction2b.setId(22L);
    stTransaction2b.setHash(txnHash2);
    stTransaction2b.setEncodedPayloadCodec(CODEC);

    StagingAffectedTransaction stAffectedContractTransaction21b = new StagingAffectedTransaction();
    stAffectedContractTransaction21b.setHash(txnHash1);

    stAffectedContractTransaction21b.setSourceTransaction(stTransaction2b);

    stTransaction2b.getAffectedContractTransactions().add(stAffectedContractTransaction21b);

    entityManager.persist(stTransaction2b);

    final String txnHash4 = Utils.createHashStr();
    // we are storing a transaction TXN4 which depends on another transaction TXN3 (which has not
    // been received yet)
    final String txnHash3 = Utils.createHashStr();

    final StagingTransaction stTransaction4 = new StagingTransaction();
    stTransaction4.setId(4L);
    stTransaction4.setHash(txnHash4);
    stTransaction4.setEncodedPayloadCodec(CODEC);

    StagingAffectedTransaction stAffectedContractTransaction43 = new StagingAffectedTransaction();
    stAffectedContractTransaction43.setHash(txnHash3);
    stAffectedContractTransaction43.setSourceTransaction(stTransaction4);

    stTransaction4.getAffectedContractTransactions().add(stAffectedContractTransaction43);

    entityManager.persist(stTransaction4);

    final StagingTransaction stTransaction3 = new StagingTransaction();
    stTransaction3.setHash(txnHash3);
    stTransaction3.setId(3L);
    stTransaction3.setEncodedPayloadCodec(CODEC);

    StagingAffectedTransaction stAffectedContractTransaction31 = new StagingAffectedTransaction();
    stAffectedContractTransaction31.setHash(txnHash1);
    stAffectedContractTransaction31.setSourceTransaction(stTransaction3);

    stTransaction3.getAffectedContractTransactions().add(stAffectedContractTransaction31);

    entityManager.persist(stTransaction3);

    final String txnHash5 = Utils.createHashStr();
    // TXN5 is a unresolvable transaction as it depends on TXN6 which is never received
    final String txnHash6 = Utils.createHashStr();

    final StagingTransaction stTransaction5 = new StagingTransaction();
    stTransaction5.setHash(txnHash5);
    stTransaction5.setId(5L);
    stTransaction5.setEncodedPayloadCodec(CODEC);

    StagingAffectedTransaction stAffectedContractTransaction56 = new StagingAffectedTransaction();
    stAffectedContractTransaction56.setHash(txnHash6);
    stAffectedContractTransaction56.setSourceTransaction(stTransaction5);

    stTransaction5.getAffectedContractTransactions().add(stAffectedContractTransaction56);

    entityManager.persist(stTransaction5);

    final String txnHash7 = Utils.createHashStr();
    // TXN7 depends on TXN1 and TXN3
    final StagingTransaction stTransaction7 = new StagingTransaction();
    stTransaction7.setHash(txnHash7);
    stTransaction7.setId(7L);
    stTransaction7.setEncodedPayloadCodec(CODEC);

    StagingAffectedTransaction stAffectedContractTransaction71 = new StagingAffectedTransaction();
    stAffectedContractTransaction71.setHash(txnHash1);
    stAffectedContractTransaction71.setSourceTransaction(stTransaction7);

    stTransaction7.getAffectedContractTransactions().add(stAffectedContractTransaction71);

    StagingAffectedTransaction stAffectedContractTransaction74 = new StagingAffectedTransaction();
    stAffectedContractTransaction74.setHash(txnHash4);
    stAffectedContractTransaction74.setSourceTransaction(stTransaction7);

    stTransaction7.getAffectedContractTransactions().add(stAffectedContractTransaction74);

    entityManager.persist(stTransaction7);

    entityManager.getTransaction().commit();

    Map<String, StagingTransaction> transactions = new HashMap<>();
    transactions.put("TXN1", stTransaction1);
    transactions.put("TXN2A", stTransaction2a);
    transactions.put("TXN2B", stTransaction2b);
    transactions.put("TXN3", stTransaction3);
    transactions.put("TXN4", stTransaction4);
    transactions.put("TXN5", stTransaction5);
    transactions.put("TXN7", stTransaction7);
    return transactions;
  }

  @Test
  public void createStagingEntityDAOFromServiceLoader() {
    try (var mockedServiceLoader = mockStatic(ServiceLoader.class)) {
      ServiceLoader serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst()).thenReturn(Optional.of(mock(StagingEntityDAO.class)));
      mockedServiceLoader
          .when(() -> ServiceLoader.load(StagingEntityDAO.class))
          .thenReturn(serviceLoader);

      StagingEntityDAO.create();

      verify(serviceLoader).findFirst();
      verifyNoMoreInteractions(serviceLoader);
      mockedServiceLoader.verify(() -> ServiceLoader.load(StagingEntityDAO.class));
      mockedServiceLoader.verifyNoMoreInteractions();
    }
  }

  @Parameterized.Parameters(name = "DB {0}")
  public static Collection<TestConfig> connectionDetails() {
    return List.of(TestConfig.values());
  }
}
