package com.quorum.tessera.data.internal;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.quorum.tessera.data.*;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
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

  private EntityManagerFactory entityManagerFactory;

  private EncryptedTransactionDAO encryptedTransactionDAO;

  private TestConfig testConfig;

  public EncryptedTransactionDAOTest(TestConfig testConfig) {
    this.testConfig = testConfig;
  }

  @Before
  public void onSetUp() {

    Map properties = new HashMap();
    properties.put("jakarta.persistence.jdbc.url", testConfig.getUrl());
    properties.put("jakarta.persistence.jdbc.user", "junit");
    properties.put("jakarta.persistence.jdbc.password", "");
    properties.put(
        "eclipselink.logging.logger", "org.eclipse.persistence.logging.slf4j.SLF4JLogger");
    properties.put("eclipselink.logging.level", "FINE");
    properties.put("eclipselink.logging.parameters", "true");
    properties.put("eclipselink.logging.level.sql", "FINEST");
    properties.put("eclipselink.cache.shared.default", "false");
    properties.put("jakarta.persistence.schema-generation.database.action", "create");

    entityManagerFactory = Persistence.createEntityManagerFactory("tessera", properties);

    encryptedTransactionDAO = new EncryptedTransactionDAOImpl(entityManagerFactory);
  }

  @After
  public void onTearDown() {

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    entityManager.createQuery("delete from EncryptedTransaction").executeUpdate();
    entityManager.getTransaction().commit();
  }

  @Test
  public void saveDoesntAllowNullEncodedPayload() {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(null);

    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

      EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
      encryptedTransaction.setHash(new MessageHash(new byte[] {5}));
      encryptedTransaction.setPayload(encodedPayload);
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
    verify(payloadEncoder).encode(encodedPayload);
  }

  @Test
  public void saveDoesntAllowNullHash() {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    byte[] payloadData = "payloadData".getBytes();
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

      EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
      encryptedTransaction.setPayload(encodedPayload);

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
  }

  @Test
  public void updateTransaction() {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    final byte[] initialData = "DATA1".getBytes();
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(initialData);
    when(payloadEncoder.decode(initialData)).thenReturn(encodedPayload);
    final byte[] updatedData = "DATA2".getBytes();
    EncodedPayload updatedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(updatedPayload)).thenReturn(updatedData);
    when(payloadEncoder.decode(updatedData)).thenReturn(updatedPayload);

    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

      final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
      encryptedTransaction.setPayload(encodedPayload);
      encryptedTransaction.setHash(new MessageHash(new byte[] {1}));

      encryptedTransactionDAO.save(encryptedTransaction);

      EntityManager entityManager = entityManagerFactory.createEntityManager();
      entityManager.getTransaction().begin();
      final EncryptedTransaction retrieved =
          entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

      assertThat(retrieved).isNotNull().usingRecursiveComparison().isEqualTo(encryptedTransaction);

      encryptedTransaction.setPayload(updatedPayload);
      encryptedTransaction.setEncodedPayload(new byte[] {0});
      encryptedTransactionDAO.update(encryptedTransaction);

      entityManager.getTransaction().rollback();

      entityManager.getTransaction().begin();

      final EncryptedTransaction after =
          entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

      assertThat(after.getPayload()).isEqualTo(updatedPayload);
      assertThat(after.getEncodedPayload()).isEqualTo(updatedData);

      entityManager.getTransaction().rollback();
    }
    verify(payloadEncoder, times(2)).encode(any());
    verify(payloadEncoder, times(3)).decode(any());
    verifyNoMoreInteractions(payloadEncoder);
  }

  @Test
  public void cannotPersistMultipleOfSameHash() {
    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    final byte[] payloadData = "PAYLOADATA".getBytes();
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

      final MessageHash messageHash = new MessageHash(new byte[] {1});

      final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
      encryptedTransaction.setPayload(encodedPayload);
      encryptedTransaction.setHash(messageHash);

      EntityManager entityManager = entityManagerFactory.createEntityManager();
      entityManager.getTransaction().begin();
      entityManager.persist(encryptedTransaction);
      entityManager.getTransaction().commit();

      final EncryptedTransaction duplicateTransaction = new EncryptedTransaction();
      duplicateTransaction.setPayload(encodedPayload);
      duplicateTransaction.setHash(messageHash);

      try {
        encryptedTransactionDAO.save(encryptedTransaction);
        failBecauseExceptionWasNotThrown(PersistenceException.class);
      } catch (PersistenceException ex) {
        assertThat(ex)
            .isInstanceOf(PersistenceException.class)
            .hasMessageContaining(testConfig.getUniqueConstraintViolationMessage());
      }
      verify(payloadEncoder, times(2)).encode(encodedPayload);
      verifyNoMoreInteractions(payloadEncoder);
    }
  }

  @Test
  public void validEncryptedTransactionCanBePersisted() {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    final byte[] payloadData = "PAYLOADATA".getBytes();
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);
    when(payloadEncoder.decode(payloadData)).thenReturn(encodedPayload);
    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

      final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
      encryptedTransaction.setPayload(encodedPayload);
      encryptedTransaction.setHash(new MessageHash(new byte[] {1}));
      encryptedTransactionDAO.save(encryptedTransaction);

      EntityManager entityManager = entityManagerFactory.createEntityManager();
      entityManager.getTransaction().begin();
      final EncryptedTransaction retrieved =
          entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());
      assertThat(retrieved).usingRecursiveComparison().isEqualTo(encryptedTransaction);

      entityManager.getTransaction().rollback();
    }
  }

  @Test
  public void fetchingAllTransactionsReturnsAll() {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    final byte[] payloadData = "I Love Sparrows".getBytes();
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);
    when(payloadEncoder.decode(payloadData)).thenReturn(encodedPayload);

    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

      EntityManager entityManager = entityManagerFactory.createEntityManager();

      entityManager.getTransaction().begin();
      final List<EncryptedTransaction> payloads =
          IntStream.range(0, 50)
              .mapToObj(i -> UUID.randomUUID().toString().getBytes())
              .map(MessageHash::new)
              .map(
                  hash -> {
                    EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
                    encryptedTransaction.setHash(hash);
                    encryptedTransaction.setPayload(encodedPayload);
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
    verify(payloadEncoder, times(50)).decode(payloadData);
    verify(payloadEncoder, times(50)).encode(encodedPayload);
    verifyNoMoreInteractions(payloadEncoder);
  }

  @Test
  public void deleteTransactionRemovesFromDatabaseAndReturnsTrue() {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    final byte[] payloadData = "PAYLOADATA".getBytes();
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

      final MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

      EntityManager entityManager = entityManagerFactory.createEntityManager();
      // put a transaction in the database
      final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
      encryptedTransaction.setPayload(encodedPayload);
      encryptedTransaction.setHash(messageHash);

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
    verify(payloadEncoder).decode(payloadData);
    verify(payloadEncoder).encode(encodedPayload);
    verifyNoMoreInteractions(payloadEncoder);
  }

  @Test(expected = EntityNotFoundException.class)
  public void deleteThrowsEntityNotFoundExceptionForNonExistentHash() {
    // delete the transaction
    final MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

    encryptedTransactionDAO.delete(messageHash);
  }

  @Test
  public void retrieveByHashFindsTransactionThatIsPresent() {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    final byte[] payloadData = "PAYLOADATA".getBytes();
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);
    when(payloadEncoder.decode(payloadData)).thenReturn(encodedPayload);

    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));
      // put a transaction in the database
      MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

      final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
      encryptedTransaction.setHash(messageHash);
      encryptedTransaction.setPayload(encodedPayload);

      EntityManager entityManager = entityManagerFactory.createEntityManager();
      entityManager.getTransaction().begin();
      entityManager.persist(encryptedTransaction);
      entityManager.getTransaction().commit();

      final Optional<EncryptedTransaction> retrieved =
          encryptedTransactionDAO.retrieveByHash(messageHash);

      assertThat(retrieved.isPresent()).isTrue();
      assertThat(retrieved.get()).usingRecursiveComparison().isEqualTo(encryptedTransaction);
    }
    verify(payloadEncoder).encode(encodedPayload);
    verify(payloadEncoder).decode(payloadData);
    verifyNoMoreInteractions(payloadEncoder);
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

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    final byte[] payloadData = "PAYLOADATA".getBytes();
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);
    when(payloadEncoder.decode(payloadData)).thenReturn(encodedPayload);
    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

      MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());
      final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
      encryptedTransaction.setPayload(encodedPayload);
      encryptedTransaction.setHash(messageHash);

      encryptedTransactionDAO.save(encryptedTransaction);

      EntityManager entityManager = entityManagerFactory.createEntityManager();
      entityManager.getTransaction().begin();
      final EncryptedTransaction retrieved =
          entityManager.find(EncryptedTransaction.class, messageHash);
      entityManager.getTransaction().commit();

      assertThat(retrieved).isNotNull();
      assertThat(retrieved.getTimestamp()).isNotZero().isGreaterThan(0L);
    }
    verify(payloadEncoder).decode(payloadData);
    verify(payloadEncoder).encode(encodedPayload);
    verifyNoMoreInteractions(payloadEncoder);
  }

  @Test
  public void findByHashes() {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    final byte[] payloadData = "PAYLOADATA".getBytes();
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

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
                    encryptedTransaction.setPayload(encodedPayload);
                    entityManager.persist(encryptedTransaction);
                    return encryptedTransaction;
                  })
              .collect(Collectors.toList());

      entityManager.getTransaction().commit();

      Collection<MessageHash> hashes =
          transactions.stream().map(EncryptedTransaction::getHash).collect(Collectors.toList());
      List<EncryptedTransaction> results = encryptedTransactionDAO.findByHashes(hashes);

      assertThat(results).isNotEmpty().containsExactlyInAnyOrderElementsOf(transactions);
      assertThat(results.stream().allMatch(r -> Arrays.equals(r.getEncodedPayload(), payloadData)))
          .isTrue();
    }
    verify(payloadEncoder, times(100)).encode(encodedPayload);
  }

  @Test
  public void findByHashesEmpty() {

    List<EncryptedTransaction> results =
        encryptedTransactionDAO.findByHashes(Collections.EMPTY_LIST);

    assertThat(results).isEmpty();
  }

  @Test
  public void saveTransactionWithCallback() throws Exception {
    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    final byte[] payloadData = UUID.randomUUID().toString().getBytes();
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);
    when(payloadEncoder.decode(payloadData)).thenReturn(encodedPayload);

    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

      MessageHash transactionHash = new MessageHash(UUID.randomUUID().toString().getBytes());
      EncryptedTransaction transaction = new EncryptedTransaction();
      transaction.setHash(transactionHash);
      transaction.setPayload(encodedPayload);

      Callable<Void> callback = mock(Callable.class);

      encryptedTransactionDAO.save(transaction, callback);

      EntityManager entityManager = entityManagerFactory.createEntityManager();
      final EncryptedTransaction result =
          entityManager.find(EncryptedTransaction.class, transactionHash);
      assertThat(result).isNotNull();
      assertThat(result.getEncodedPayload()).containsExactly(payloadData);
      assertThat(result.getHash()).isEqualTo(transactionHash);

      verify(callback).call();
    }

    verify(payloadEncoder).encode(encodedPayload);
    verify(payloadEncoder).decode(payloadData);
  }

  @Test
  public void saveTransactionWithCallbackException() throws Exception {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    byte[] payloadData = "payloadData".getBytes();
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);
    when(payloadEncoder.decode(payloadData)).thenReturn(encodedPayload);

    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

      MessageHash transactionHash = new MessageHash(UUID.randomUUID().toString().getBytes());
      EncryptedTransaction transaction = new EncryptedTransaction();
      transaction.setPayload(encodedPayload);
      transaction.setHash(transactionHash);

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
    verify(payloadEncoder).encode(encodedPayload);
    verifyNoMoreInteractions(payloadEncoder);
  }

  @Test
  public void saveTransactionWithCallbackRuntimeException() throws Exception {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    final byte[] payloadData = "PAYLOADATA".getBytes();
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);
    when(payloadEncoder.decode(payloadData)).thenReturn(encodedPayload);

    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

      MessageHash transactionHash = new MessageHash(UUID.randomUUID().toString().getBytes());
      EncryptedTransaction transaction = new EncryptedTransaction();
      transaction.setHash(transactionHash);
      transaction.setPayload(encodedPayload);

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
    verify(payloadEncoder).encode(encodedPayload);
    verifyNoMoreInteractions(payloadEncoder);
  }

  @Test
  public void callBackShouldNotBeExecutedIfSaveFails() {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    final byte[] payloadData = "PAYLOADATA".getBytes();
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

    try (var createEncoderFunction = mockStatic(PayloadEncoder.class)) {
      createEncoderFunction
          .when(() -> PayloadEncoder.create(EncodedPayloadCodec.current()))
          .thenReturn(Optional.of(payloadEncoder));

      final MessageHash messageHash = new MessageHash(new byte[] {1});
      final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
      encryptedTransaction.setPayload(encodedPayload);
      encryptedTransaction.setHash(messageHash);

      EntityManager entityManager = entityManagerFactory.createEntityManager();
      entityManager.getTransaction().begin();
      entityManager.persist(encryptedTransaction);
      entityManager.getTransaction().commit();
      entityManager.clear();

      assertThat(encryptedTransaction.getEncodedPayloadCodec())
          .isEqualTo(EncodedPayloadCodec.current());
      assertThat(encryptedTransaction.getEncodedPayload()).containsExactly(payloadData);

      final EncryptedTransaction duplicateTransaction = new EncryptedTransaction();
      duplicateTransaction.setPayload(encodedPayload);
      duplicateTransaction.setHash(messageHash);
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
    verify(payloadEncoder, times(2)).encode(encodedPayload);
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
    return List.of(TestConfig.values());
  }
}
