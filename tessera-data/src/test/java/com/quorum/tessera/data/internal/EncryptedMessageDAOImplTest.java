package com.quorum.tessera.data.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.data.*;
import jakarta.persistence.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class EncryptedMessageDAOImplTest {
  private EntityManagerFactory entityManagerFactory;

  private EncryptedMessageDAO encryptedMessageDAO;

  private TestConfig testConfig;

  public EncryptedMessageDAOImplTest(TestConfig testConfig) {
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

    encryptedMessageDAO = new EncryptedMessageDAOImpl(entityManagerFactory);
  }

  @Parameterized.Parameters(name = "DB {0}")
  public static Collection<TestConfig> connectionDetails() {
    return List.of(TestConfig.values());
  }

  @Test
  public void testSave() {
    EncryptedMessageDAOImpl impl = new EncryptedMessageDAOImpl(entityManagerFactory);
    EncryptedMessage encryptedMessage = new EncryptedMessage();
    MessageHash messageHash = new MessageHash("test".getBytes());
    encryptedMessage.setHash(messageHash);
    encryptedMessage.setContent("test".getBytes());
    encryptedMessage.setTimestamp(System.currentTimeMillis());
    try {
      impl.save(encryptedMessage);
    } catch (PersistenceException ex) {
      assertThat(ex).isInstanceOf(PersistenceException.class);
    }

    Optional<EncryptedMessage> optional = impl.retrieveByHash(messageHash);
    EncryptedMessage encryptedMessage1 = optional.get();
    assertThat(encryptedMessage1.getContent()).isNotNull();
    assertThat(encryptedMessage1.getTimestamp()).isNotNull();

    List<MessageHash> list = impl.retrieveMessageHashes(0, 1);
    assertThat(list).isNotNull();

    assertThat(impl.messageCount()).isEqualTo(1);
  }

  @Test
  public void upcheckReturnsTrue() {
    EncryptedMessageDAOImpl impl = new EncryptedMessageDAOImpl(entityManagerFactory);
    assertThat(impl.upcheck());
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

    EncryptedMessageDAOImpl impl = new EncryptedMessageDAOImpl(mockEntityManagerFactory);

    assertThat(impl.upcheck()).isFalse();
  }

  @Test
  public void delete() {
    EncryptedMessageDAOImpl impl = new EncryptedMessageDAOImpl(entityManagerFactory);
    EncryptedMessage encryptedMessage = new EncryptedMessage();
    MessageHash messageHash = new MessageHash("test".getBytes());
    encryptedMessage.setHash(messageHash);
    encryptedMessage.setContent("test".getBytes());
    encryptedMessage.setTimestamp(System.currentTimeMillis());
    try {
      impl.save(encryptedMessage);
    } catch (PersistenceException ex) {
      assertThat(ex).isInstanceOf(PersistenceException.class);
    }

    impl.delete(messageHash);
    Optional<EncryptedMessage> optional = impl.retrieveByHash(messageHash);
    assertThat(optional.isEmpty()).isTrue();
  }

  @Test
  public void findByHashes() {
    EncryptedMessageDAOImpl impl = new EncryptedMessageDAOImpl(entityManagerFactory);
    EncryptedMessage encryptedMessage = new EncryptedMessage();
    MessageHash messageHash = new MessageHash("test".getBytes());
    encryptedMessage.setHash(messageHash);
    encryptedMessage.setContent("test".getBytes());
    encryptedMessage.setTimestamp(System.currentTimeMillis());
    try {
      impl.save(encryptedMessage);
    } catch (PersistenceException ex) {
      assertThat(ex).isInstanceOf(PersistenceException.class);
    }

    EncryptedMessageDAOImpl impl2 = new EncryptedMessageDAOImpl(entityManagerFactory);
    EncryptedMessage encryptedMessage2 = new EncryptedMessage();
    MessageHash messageHash2 = new MessageHash("test".getBytes());
    encryptedMessage.setHash(messageHash2);
    encryptedMessage.setContent("test".getBytes());
    encryptedMessage.setTimestamp(System.currentTimeMillis());
    try {
      impl2.save(encryptedMessage2);
    } catch (PersistenceException ex) {
      assertThat(ex).isInstanceOf(PersistenceException.class);
    }

    EncryptedMessageDAOImpl ob = new EncryptedMessageDAOImpl(entityManagerFactory);
    List<MessageHash> list = Arrays.asList(messageHash, messageHash2);
    List<EncryptedMessage> msgList = ob.findByHashes(list);
    assertThat(msgList).isNotNull();

    List<MessageHash> emptyList = new ArrayList<>();
    List<EncryptedMessage> msgsList = ob.findByHashes(emptyList);
    assertThat(msgsList).isEmpty();
  }
}
