package com.quorum.tessera.data.internal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.*;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import jakarta.persistence.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class EncryptedTransactionDAOTest {

  private final EncodedPayloadCodec encodedPayloadCodec = EncodedPayloadCodec.UNSUPPORTED;

  private EntityManagerFactory entityManagerFactory;

  private EncryptedTransactionDAO encryptedTransactionDAO;

  private TestConfig testConfig;

  public EncryptedTransactionDAOTest(TestConfig testConfig) {
    this.testConfig = testConfig;
  }

  @Before
  public void onSetUp() {
    System.setProperty("disable.jpa.listeners", "true");
    Map properties = new HashMap();
    properties.put("jakarta.persistence.jdbc.url", testConfig.getUrl());
    properties.put("jakarta.persistence.jdbc.user", "junit");
    properties.put("jakarta.persistence.jdbc.password", "");
    properties.put(
        "eclipselink.logging.logger", "org.eclipse.persistence.logging.slf4j.SLF4JLogger");
    properties.put("eclipselink.logging.level", "FINE");
    properties.put("eclipselink.logging.parameters", "true");
    properties.put("eclipselink.logging.level.sql", "FINE");
    properties.put("eclipselink.cache.shared.default", "false");
    properties.put("jakarta.persistence.schema-generation.database.action", "create");

    entityManagerFactory = Persistence.createEntityManagerFactory("tessera", properties);

    encryptedTransactionDAO = new EncryptedTransactionDAOImpl(entityManagerFactory);
  }

  @After
  public void onTearDown() {
    System.clearProperty("disable.jpa.listeners");
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    entityManager.getTransaction().begin();
    entityManager.createQuery("delete from EncryptedTransaction").executeUpdate();
    entityManager.getTransaction().commit();
  }

  @Test
  public void saveDoesntAllowNullEncodedPayload() {

    EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setHash(new MessageHash(new byte[] {5}));

    try {
      encryptedTransactionDAO.save(encryptedTransaction);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      String expectedMessage =
          String.format(testConfig.getRequiredFieldColumnTemplate(), "ENCODED_PAYLOAD");

      assertThat(ex)
          .isInstanceOf(PersistenceException.class)
          .hasMessageContaining(expectedMessage)
          .hasMessageContaining("ENCODED_PAYLOAD");
    }
  }

  @Test
  public void saveDoesntAllowNullHash() {

    EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setEncodedPayload(new byte[] {5});
    encryptedTransaction.setEncodedPayloadCodec(encodedPayloadCodec);
    try {
      encryptedTransactionDAO.save(encryptedTransaction);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      String expectedMessage = String.format(testConfig.getRequiredFieldColumnTemplate(), "HASH");

      assertThat(ex)
          .isInstanceOf(PersistenceException.class)
          .hasMessageContaining(expectedMessage)
          .hasMessageContaining("HASH");
    }
  }

  @Test
  public void updateTransaction() {

    final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setEncodedPayload(new byte[] {5});
    encryptedTransaction.setHash(new MessageHash(new byte[] {1}));
    encryptedTransaction.setEncodedPayloadCodec(EncodedPayloadCodec.UNSUPPORTED);

    encryptedTransactionDAO.save(encryptedTransaction);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    final EncryptedTransaction retrieved =
        entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

    assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedTransaction);

    encryptedTransaction.setEncodedPayload(new byte[] {6});
    encryptedTransactionDAO.update(encryptedTransaction);

    entityManager.getTransaction().rollback();

    entityManager.getTransaction().begin();

    final EncryptedTransaction after =
        entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

    assertThat(after).isEqualToComparingFieldByField(encryptedTransaction);

    entityManager.getTransaction().rollback();
  }

  @Test
  public void cannotPersistMultipleOfSameHash() {

    final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setEncodedPayload(new byte[] {5});
    encryptedTransaction.setHash(new MessageHash(new byte[] {1}));
    encryptedTransaction.setEncodedPayloadCodec(encodedPayloadCodec);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    entityManager.persist(encryptedTransaction);
    entityManager.getTransaction().commit();

    final EncryptedTransaction duplicateTransaction = new EncryptedTransaction();
    duplicateTransaction.setEncodedPayload(new byte[] {6});
    duplicateTransaction.setHash(new MessageHash(new byte[] {1}));

    try {
      encryptedTransactionDAO.save(encryptedTransaction);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      assertThat(ex)
          .isInstanceOf(PersistenceException.class)
          .hasMessageContaining(testConfig.getUniqueConstraintViolationMessage());
    }
  }

  @Test
  public void validEncryptedTransactionCanBePersisted() {

    final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setEncodedPayload(new byte[] {5});
    encryptedTransaction.setHash(new MessageHash(new byte[] {1}));
    encryptedTransaction.setEncodedPayloadCodec(EncodedPayloadCodec.UNSUPPORTED);
    encryptedTransactionDAO.save(encryptedTransaction);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    final EncryptedTransaction retrieved =
        entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

    assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedTransaction);
    entityManager.getTransaction().rollback();
  }

  @Test
  public void fetchingAllTransactionsReturnsAll() {

    EntityManager entityManager = entityManagerFactory.createEntityManager();

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getEncodedPayloadCodec()).thenReturn(EncodedPayloadCodec.UNSUPPORTED);

    byte[] payloadData = "I Love Sparrows".getBytes();

    entityManager.getTransaction().begin();
    final List<EncryptedTransaction> payloads =
        IntStream.range(0, 50)
            .mapToObj(i -> UUID.randomUUID().toString().getBytes())
            .map(MessageHash::new)
            .map(
                hash -> {
                  EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
                  encryptedTransaction.setHash(hash);
                  encryptedTransaction.setEncodedPayload(payloadData);
                  encryptedTransaction.setPayload(encodedPayload);
                  encryptedTransaction.setEncodedPayloadCodec(EncodedPayloadCodec.UNSUPPORTED);
                  return encryptedTransaction;
                })
            .peek(entityManager::persist)
            .collect(Collectors.toList());

    entityManager.getTransaction().commit();

    final List<EncryptedTransaction> retrievedList =
        encryptedTransactionDAO.retrieveTransactions(0, Integer.MAX_VALUE);

    assertThat(encryptedTransactionDAO.transactionCount()).isEqualTo(payloads.size());
    assertThat(retrievedList).hasSameSizeAs(payloads);
    assertThat(retrievedList).hasSameElementsAs(payloads);
  }

  @Test
  public void deleteTransactionRemovesFromDatabaseAndReturnsTrue() {

    final MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    // put a transaction in the database
    final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setEncodedPayload(new byte[] {5});
    encryptedTransaction.setHash(messageHash);
    encryptedTransaction.setEncodedPayloadCodec(EncodedPayloadCodec.LEGACY);

    entityManager.getTransaction().begin();
    entityManager.persist(encryptedTransaction);
    entityManager.getTransaction().commit();

    Query countQuery =
        entityManager.createQuery(
            "select count(t) from EncryptedTransaction t where t.hash = :hash");
    Long result = (Long) countQuery.setParameter("hash", messageHash).getSingleResult();
    assertThat(result).isEqualTo(1L);

    encryptedTransactionDAO.delete(messageHash);

    // check it is not longer in the database
    Long result2 = (Long) countQuery.setParameter("hash", messageHash).getSingleResult();
    assertThat(result2).isZero();
  }

  @Test(expected = EntityNotFoundException.class)
  public void deleteThrowsEntityNotFoundExceptionForNonExistentHash() {
    // delete the transaction
    final MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

    encryptedTransactionDAO.delete(messageHash);
  }

  @Test
  public void retrieveByHashFindsTransactionThatIsPresent() {
    // put a transaction in the database
    MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

    final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setEncodedPayload(new byte[] {5});
    encryptedTransaction.setHash(messageHash);
    encryptedTransaction.setEncodedPayloadCodec(EncodedPayloadCodec.UNSUPPORTED);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    entityManager.persist(encryptedTransaction);
    entityManager.getTransaction().commit();

    final Optional<EncryptedTransaction> retrieved =
        encryptedTransactionDAO.retrieveByHash(messageHash);

    assertThat(retrieved.isPresent()).isTrue();
    assertThat(retrieved.get()).isEqualToComparingFieldByField(encryptedTransaction);
  }

  @Test
  public void retrieveByHashThrowsExceptionWhenNotPresent() {
    MessageHash searchHash = new MessageHash(UUID.randomUUID().toString().getBytes());

    final Optional<EncryptedTransaction> retrieved =
        encryptedTransactionDAO.retrieveByHash(searchHash);

    assertThat(retrieved.isPresent()).isFalse();
  }

  @Test
  public void persistAddsTimestampToEntity() {
    MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());
    final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setEncodedPayload(new byte[] {5});
    encryptedTransaction.setHash(messageHash);
    encryptedTransaction.setEncodedPayloadCodec(EncodedPayloadCodec.UNSUPPORTED);

    final long expected = System.currentTimeMillis();
    encryptedTransactionDAO.save(encryptedTransaction);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    final EncryptedTransaction retrieved =
        entityManager.find(EncryptedTransaction.class, messageHash);
    entityManager.getTransaction().commit();

    assertThat(retrieved).isNotNull();
    assertThat(retrieved.getTimestamp()).isNotZero();
  }

  @Test
  public void findByHashes() {

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    List<EncryptedTransaction> transactions =
        IntStream.range(0, 100)
            .mapToObj(i -> UUID.randomUUID().toString().getBytes())
            .map(MessageHash::new)
            .map(
                h -> {
                  EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
                  encryptedTransaction.setHash(h);
                  encryptedTransaction.setEncodedPayload(UUID.randomUUID().toString().getBytes());
                  encryptedTransaction.setEncodedPayloadCodec(EncodedPayloadCodec.UNSUPPORTED);
                  entityManager.persist(encryptedTransaction);
                  return encryptedTransaction;
                })
            .collect(Collectors.toList());

    entityManager.getTransaction().commit();

    Collection<MessageHash> hashes =
        transactions.stream().map(EncryptedTransaction::getHash).collect(Collectors.toList());
    List<EncryptedTransaction> results = encryptedTransactionDAO.findByHashes(hashes);

    assertThat(results).isNotEmpty().containsExactlyInAnyOrderElementsOf(transactions);
  }

  @Test
  public void findByHashesEmpty() {

    List<EncryptedTransaction> results =
        encryptedTransactionDAO.findByHashes(Collections.EMPTY_LIST);

    assertThat(results).isEmpty();
  }

  @Test
  public void saveTransactionWithCallback() throws Exception {

    MessageHash transactionHash = new MessageHash(UUID.randomUUID().toString().getBytes());
    EncryptedTransaction transaction = new EncryptedTransaction();
    transaction.setHash(transactionHash);
    transaction.setEncodedPayload(UUID.randomUUID().toString().getBytes());
    transaction.setEncodedPayloadCodec(EncodedPayloadCodec.UNSUPPORTED);

    Callable<Void> callback = mock(Callable.class);

    encryptedTransactionDAO.save(transaction, callback);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    final EncryptedTransaction result =
        entityManager.find(EncryptedTransaction.class, transactionHash);
    assertThat(result).isNotNull();

    verify(callback).call();
  }

  @Test
  public void saveTransactionWithCallbackException() throws Exception {

    MessageHash transactionHash = new MessageHash(UUID.randomUUID().toString().getBytes());
    EncryptedTransaction transaction = new EncryptedTransaction();
    transaction.setEncodedPayloadCodec(encodedPayloadCodec);
    transaction.setHash(transactionHash);
    transaction.setEncodedPayload(UUID.randomUUID().toString().getBytes());

    Callable<Void> callback = mock(Callable.class);
    when(callback.call()).thenThrow(new Exception("OUCH"));

    try {
      encryptedTransactionDAO.save(transaction, callback);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      assertThat(ex).isNotNull().hasMessageContaining("OUCH");
    }

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    final EncryptedTransaction result =
        entityManager.find(EncryptedTransaction.class, transactionHash);
    assertThat(result).isNull();

    verify(callback).call();
  }

  @Test
  public void saveTransactionWithCallbackRuntimeException() throws Exception {

    MessageHash transactionHash = new MessageHash(UUID.randomUUID().toString().getBytes());
    EncryptedTransaction transaction = new EncryptedTransaction();
    transaction.setHash(transactionHash);
    transaction.setEncodedPayload(UUID.randomUUID().toString().getBytes());
    transaction.setEncodedPayloadCodec(encodedPayloadCodec);

    Callable<Void> callback = mock(Callable.class);
    when(callback.call()).thenThrow(new RuntimeException("OUCH"));

    try {
      encryptedTransactionDAO.save(transaction, callback);
      failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException ex) {
      assertThat(ex).isNotNull().hasMessageContaining("OUCH");
    }

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    final EncryptedTransaction result =
        entityManager.find(EncryptedTransaction.class, transactionHash);
    assertThat(result).isNull();

    verify(callback).call();
  }

  @Test
  public void callBackShouldNotBeExecutedIfSaveFails() {
    final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setEncodedPayloadCodec(encodedPayloadCodec);
    encryptedTransaction.setEncodedPayload(new byte[] {5});
    encryptedTransaction.setHash(new MessageHash(new byte[] {1}));
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    entityManager.persist(encryptedTransaction);
    entityManager.getTransaction().commit();
    entityManager.clear();
    final EncryptedTransaction duplicateTransaction = new EncryptedTransaction();
    duplicateTransaction.setEncodedPayload(new byte[] {6});
    duplicateTransaction.setHash(new MessageHash(new byte[] {1}));
    AtomicInteger count = new AtomicInteger(0);
    try {
      encryptedTransactionDAO.save(
          encryptedTransaction,
          () -> {
            count.incrementAndGet();
            return true;
          });
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      assertThat(ex)
          .isInstanceOf(PersistenceException.class)
          .hasMessageContaining(testConfig.getUniqueConstraintViolationMessage());
      assertThat(count.get()).isEqualTo(0);
    }
  }

  @Test
  public void upcheckReturnsTrue() {
    assertThat(encryptedTransactionDAO.upcheck());
  }

  @Test
  public void upcheckFailDueToDB() {
    EntityManagerFactory mockEntityManagerFactory = mock(EntityManagerFactory.class);
    EntityManager mockEntityManager = mock(EntityManager.class);
    EntityTransaction mockEntityTransaction = mock(EntityTransaction.class);
    EntityManagerCallback mockEntityManagerCallback = mock(EntityManagerCallback.class);

    when(mockEntityManagerFactory.createEntityManager()).thenReturn(mockEntityManager);
    when(mockEntityManager.getTransaction()).thenReturn(mockEntityTransaction);
    when(mockEntityManagerCallback.execute(mockEntityManager)).thenThrow(RuntimeException.class);

    EncryptedTransactionDAO encryptedTransactionDAO =
        new EncryptedTransactionDAOImpl(mockEntityManagerFactory);

    assertThat(encryptedTransactionDAO.upcheck()).isFalse();
  }

  @Test
  public void create() {
    try (var mockedServiceLoader = mockStatic(ServiceLoader.class)) {

      ServiceLoader serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst()).thenReturn(Optional.of(mock(EncryptedTransactionDAO.class)));

      mockedServiceLoader
          .when(() -> ServiceLoader.load(EncryptedTransactionDAO.class))
          .thenReturn(serviceLoader);

      EncryptedTransactionDAO.create();

      mockedServiceLoader.verify(() -> ServiceLoader.load(EncryptedTransactionDAO.class));
      mockedServiceLoader.verifyNoMoreInteractions();
      verify(serviceLoader).findFirst();
      verifyNoMoreInteractions(serviceLoader);
    }
  }

  @Parameterized.Parameters(name = "DB {0}")
  public static Collection<TestConfig> connectionDetails() {
    return List.of(TestConfig.H2, TestConfig.HSQL);
  }
}
