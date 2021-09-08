package com.quorum.tessera.data.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.*;
import jakarta.persistence.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class EncryptedRawTransactionDAOTest {

  private EntityManagerFactory entityManagerFactory;

  private EncryptedRawTransactionDAO encryptedRawTransactionDAO;

  private TestConfig testConfig;

  public EncryptedRawTransactionDAOTest(TestConfig testConfig) {
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
    properties.put("eclipselink.logging.level.sql", "FINE");
    properties.put("jakarta.persistence.schema-generation.database.action", "drop-and-create");

    entityManagerFactory = Persistence.createEntityManagerFactory("tessera", properties);

    encryptedRawTransactionDAO = new EncryptedRawTransactionDAOImpl(entityManagerFactory);
  }

  @After
  public void onTearDown() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    entityManager.createQuery("delete from EncryptedRawTransaction").executeUpdate();
    entityManager.getTransaction().commit();
  }

  @Test
  public void saveDoesntAllowNullEncyptedPayload() {

    EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
    encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
    encryptedRawTransaction.setEncryptedKey("key".getBytes());
    encryptedRawTransaction.setNonce("nonce".getBytes());
    encryptedRawTransaction.setSender("from".getBytes());

    try {
      encryptedRawTransactionDAO.save(encryptedRawTransaction);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      String expectedMessage =
          String.format(testConfig.getRequiredFieldColumnTemplate(), "ENCRYPTED_PAYLOAD");
      assertThat(ex)
          .hasMessageContaining(expectedMessage)
          .hasMessageContaining("ENCRYPTED_PAYLOAD");
    }
  }

  @Test
  public void saveDoesntAllowNullHash() {

    EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
    encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
    encryptedRawTransaction.setEncryptedKey("key".getBytes());
    encryptedRawTransaction.setNonce("nonce".getBytes());
    encryptedRawTransaction.setSender("from".getBytes());

    try {
      encryptedRawTransactionDAO.save(encryptedRawTransaction);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      String expectedMessage = String.format(testConfig.getRequiredFieldColumnTemplate(), "HASH");
      assertThat(ex).hasMessageContaining(expectedMessage).hasMessageContaining("HASH");
    }
  }

  @Test
  public void saveDoesntAllowNullNonce() {

    EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
    encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
    encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
    encryptedRawTransaction.setEncryptedKey("key".getBytes());
    encryptedRawTransaction.setSender("from".getBytes());

    try {
      encryptedRawTransactionDAO.save(encryptedRawTransaction);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      String expectedMessage = String.format(testConfig.getRequiredFieldColumnTemplate(), "NONCE");
      assertThat(ex).hasMessageContaining(expectedMessage).hasMessageContaining("NONCE");
    }
  }

  @Test
  public void saveDoesntAllowNullEncryptedKey() {

    EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
    encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
    encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
    encryptedRawTransaction.setNonce("nonce".getBytes());
    encryptedRawTransaction.setSender("from".getBytes());

    try {
      encryptedRawTransactionDAO.save(encryptedRawTransaction);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      String expectedMessage =
          String.format(testConfig.getRequiredFieldColumnTemplate(), "ENCRYPTED_KEY");
      assertThat(ex).hasMessageContaining(expectedMessage).hasMessageContaining("ENCRYPTED_KEY");
    }
  }

  @Test
  public void saveDoesntAllowNullSender() {

    EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
    encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
    encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
    encryptedRawTransaction.setNonce("nonce".getBytes());
    encryptedRawTransaction.setEncryptedKey("key".getBytes());

    try {
      encryptedRawTransactionDAO.save(encryptedRawTransaction);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      String expectedMessage = String.format(testConfig.getRequiredFieldColumnTemplate(), "SENDER");
      assertThat(ex).hasMessageContaining(expectedMessage).hasMessageContaining("SENDER");
    }
  }

  @Test
  public void cannotPersistMultipleOfSameHash() {

    final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
    encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
    encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
    encryptedRawTransaction.setEncryptedKey("key".getBytes());
    encryptedRawTransaction.setNonce("nonce".getBytes());
    encryptedRawTransaction.setSender("from".getBytes());
    encryptedRawTransactionDAO.save(encryptedRawTransaction);

    final EncryptedRawTransaction duplicateTransaction = new EncryptedRawTransaction();
    duplicateTransaction.setEncryptedPayload(new byte[] {6});
    duplicateTransaction.setHash(new MessageHash(new byte[] {1}));
    duplicateTransaction.setEncryptedKey("key".getBytes());
    duplicateTransaction.setNonce("nonce".getBytes());
    duplicateTransaction.setSender("from".getBytes());

    try {
      encryptedRawTransactionDAO.save(duplicateTransaction);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      assertThat(ex).hasMessageContaining(testConfig.getUniqueConstraintViolationMessage());
    }
  }

  @Test
  public void validEncryptedRawTransactionCanBePersisted() {

    MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

    final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
    encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
    encryptedRawTransaction.setHash(messageHash);
    encryptedRawTransaction.setEncryptedKey("key".getBytes());
    encryptedRawTransaction.setNonce("nonce".getBytes());
    encryptedRawTransaction.setSender("from".getBytes());
    encryptedRawTransactionDAO.save(encryptedRawTransaction);

    EntityManager entityManager = entityManagerFactory.createEntityManager();

    final EncryptedRawTransaction retrieved =
        entityManager.find(EncryptedRawTransaction.class, messageHash);

    assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedRawTransaction);
  }

  @Test
  public void deleteTransactionRemovesFromDatabaseAndReturnsTrue() {

    // put a transaction in the database
    MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

    final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
    encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
    encryptedRawTransaction.setHash(messageHash);
    encryptedRawTransaction.setEncryptedKey("key".getBytes());
    encryptedRawTransaction.setNonce("nonce".getBytes());
    encryptedRawTransaction.setSender("from".getBytes());

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    entityManager.persist(encryptedRawTransaction);
    entityManager.getTransaction().commit();

    Query countQuery =
        entityManager.createQuery(
            "select count(t) from EncryptedRawTransaction t where t.hash = :hash");

    Long result = (Long) countQuery.setParameter("hash", messageHash).getSingleResult();

    assertThat(result).isEqualTo(1L);

    // delete the transaction
    encryptedRawTransactionDAO.delete(messageHash);

    Long result2 = (Long) countQuery.setParameter("hash", messageHash).getSingleResult();

    assertThat(result2).isZero();
  }

  @Test(expected = EntityNotFoundException.class)
  public void deleteThrowsEntityNotFoundExceptionForNonExistentHash() {
    // delete the transaction
    encryptedRawTransactionDAO.delete(new MessageHash(UUID.randomUUID().toString().getBytes()));
  }

  @Test
  public void fetchingAllTransactionsReturnsAll() {

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    final List<EncryptedRawTransaction> payloads =
        IntStream.range(0, 50)
            .mapToObj(i -> UUID.randomUUID().toString().getBytes())
            .map(MessageHash::new)
            .map(
                hash ->
                    new EncryptedRawTransaction(
                        hash,
                        "payload".getBytes(),
                        "key".getBytes(),
                        "nonce".getBytes(),
                        "sender".getBytes()))
            .peek(entityManager::persist)
            .collect(Collectors.toList());

    entityManager.getTransaction().commit();

    final List<EncryptedRawTransaction> retrievedList =
        encryptedRawTransactionDAO.retrieveTransactions(0, Integer.MAX_VALUE);

    assertThat(encryptedRawTransactionDAO.transactionCount()).isEqualTo(payloads.size());
    assertThat(retrievedList).hasSameSizeAs(payloads);
    assertThat(retrievedList).hasSameElementsAs(payloads);
  }

  @Test
  public void retrieveByHashFindsTransactionThatIsPresent() {
    // put a transaction in the database
    MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

    final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
    encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
    encryptedRawTransaction.setHash(messageHash);
    encryptedRawTransaction.setEncryptedKey("key".getBytes());
    encryptedRawTransaction.setNonce("nonce".getBytes());
    encryptedRawTransaction.setSender("from".getBytes());

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    entityManager.persist(encryptedRawTransaction);
    entityManager.getTransaction().commit();

    final Optional<EncryptedRawTransaction> retrieved =
        encryptedRawTransactionDAO.retrieveByHash(messageHash);

    assertThat(retrieved).isPresent();
    assertThat(retrieved.get()).isEqualToComparingFieldByField(encryptedRawTransaction);
  }

  @Test
  public void retrieveByHashReturnsEmptyOptionalWhenNotPresent() {
    final MessageHash searchHash = new MessageHash(UUID.randomUUID().toString().getBytes());

    final Optional<EncryptedRawTransaction> retrieved =
        encryptedRawTransactionDAO.retrieveByHash(searchHash);

    assertThat(retrieved.isPresent()).isFalse();
  }

  @Test
  public void persistAddsTimestampToEntity() {

    final MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

    final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
    encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
    encryptedRawTransaction.setHash(messageHash);
    encryptedRawTransaction.setEncryptedKey("key".getBytes());
    encryptedRawTransaction.setNonce("nonce".getBytes());
    encryptedRawTransaction.setSender("from".getBytes());

    encryptedRawTransactionDAO.save(encryptedRawTransaction);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    final EncryptedRawTransaction retrieved =
        entityManager.find(EncryptedRawTransaction.class, messageHash);

    assertThat(retrieved).isNotNull();
    assertThat(retrieved.getTimestamp()).isNotZero();
  }

  @Test
  public void upcheckReturnsTrue() {
    assertThat(encryptedRawTransactionDAO.upcheck());
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

    EncryptedRawTransactionDAO encryptedRawTransactionDAO =
        new EncryptedRawTransactionDAOImpl(mockEntityManagerFactory);

    assertThat(encryptedRawTransactionDAO.upcheck()).isFalse();
  }

  @Test
  public void create() {
    try (var mockedServiceLoader = mockStatic(ServiceLoader.class)) {

      ServiceLoader serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst())
          .thenReturn(Optional.of(mock(EncryptedRawTransactionDAO.class)));

      mockedServiceLoader
          .when(() -> ServiceLoader.load(EncryptedRawTransactionDAO.class))
          .thenReturn(serviceLoader);

      EncryptedRawTransactionDAO.create();

      mockedServiceLoader.verify(() -> ServiceLoader.load(EncryptedRawTransactionDAO.class));
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
